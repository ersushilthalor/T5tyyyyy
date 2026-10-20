package com.hinnka.mycamera.camera

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.ImageFormat
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.MeteringRectangle
import android.media.ImageReader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import android.util.Range
import android.view.Surface
import androidx.exifinterface.media.ExifInterface
import com.hinnka.mycamera.model.CameraLens
import com.hinnka.mycamera.model.CapturedPhoto
import com.hinnka.mycamera.model.FilmRecipe
import com.hinnka.mycamera.model.FlashMode
import com.hinnka.mycamera.model.FocusMode
import com.hinnka.mycamera.model.PasmMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

class Camera2Engine(private val context: Context) : SensorEventListener {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private var backgroundThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null

    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private var previewRequestBuilder: CaptureRequest.Builder? = null
    private var imageReader: ImageReader? = null
    private var previewSurface: Surface? = null

    // State flows
    private val _isCameraReady = MutableStateFlow(false)
    val isCameraReady: StateFlow<Boolean> = _isCameraReady.asStateFlow()

    private val _histogramData = MutableStateFlow(FloatArray(32))
    val histogramData: StateFlow<FloatArray> = _histogramData.asStateFlow()

    private val _horizonRollAngle = MutableStateFlow(0f)
    val horizonRollAngle: StateFlow<Float> = _horizonRollAngle.asStateFlow()

    private val _currentIso = MutableStateFlow(100)
    val currentIso: StateFlow<Int> = _currentIso.asStateFlow()

    private val _currentShutterSpeed = MutableStateFlow(1.0 / 125.0)
    val currentShutterSpeed: StateFlow<Double> = _currentShutterSpeed.asStateFlow()

    private val _currentAperture = MutableStateFlow(1.8f)
    val currentAperture: StateFlow<Float> = _currentAperture.asStateFlow()

    private val _afLocked = MutableStateFlow(false)
    val afLocked: StateFlow<Boolean> = _afLocked.asStateFlow()

    // Config state
    private var currentCameraId: String = ""
    private var cameraCharacteristics: CameraCharacteristics? = null
    private var pasmMode: PasmMode = PasmMode.PROGRAM
    private var manualIso: Int = 100
    private var manualShutterSeconds: Double = 1.0 / 125.0
    private var evCompensation: Int = 0
    private var focusMode: FocusMode = FocusMode.AF_CONTINUOUS
    private var manualFocusDistance: Float = 0.0f
    private var flashMode: FlashMode = FlashMode.OFF
    private var currentLens: CameraLens = CameraLens.WIDE
    private var activeRecipe: FilmRecipe = FilmRecipe.Provia

    fun start() {
        startBackgroundThread()
        sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        } ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        closeCamera()
        stopBackgroundThread()
        sensorManager?.unregisterListener(this)
    }

    private fun startBackgroundThread() {
        if (backgroundThread == null) {
            backgroundThread = HandlerThread("Camera2Background").apply {
                start()
                backgroundHandler = Handler(looper)
            }
        }
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        runCatching { backgroundThread?.join() }
        backgroundThread = null
        backgroundHandler = null
    }

    fun setPreviewSurface(surface: Surface?) {
        previewSurface = surface
        if (surface != null && cameraDevice != null) {
            createCameraPreviewSession()
        }
    }

    @SuppressLint("MissingPermission")
    fun openCamera(lens: CameraLens = currentLens) {
        currentLens = lens
        val targetFacing = if (lens == CameraLens.FRONT) {
            CameraCharacteristics.LENS_FACING_FRONT
        } else {
            CameraCharacteristics.LENS_FACING_BACK
        }

        val cameraIdList = cameraManager.cameraIdList
        var selectedCameraId = cameraIdList.firstOrNull() ?: return

        for (id in cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (facing == targetFacing) {
                selectedCameraId = id
                cameraCharacteristics = characteristics
                break
            }
        }

        currentCameraId = selectedCameraId
        val characteristics = cameraCharacteristics ?: cameraManager.getCameraCharacteristics(currentCameraId)
        cameraCharacteristics = characteristics

        // Update default aperture
        characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES)?.firstOrNull()?.let {
            _currentAperture.value = it
        }

        setupImageReader(characteristics)

        try {
            cameraManager.openCamera(currentCameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    _isCameraReady.value = true
                    if (previewSurface != null) {
                        createCameraPreviewSession()
                    }
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    cameraDevice = null
                    _isCameraReady.value = false
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e("Camera2Engine", "Camera open error: $error")
                    camera.close()
                    cameraDevice = null
                    _isCameraReady.value = false
                }
            }, backgroundHandler)
        } catch (e: Exception) {
            Log.e("Camera2Engine", "Exception opening camera", e)
        }
    }

    private fun setupImageReader(characteristics: CameraCharacteristics) {
        val map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
        val jpegSizes = map?.getOutputSizes(ImageFormat.JPEG) ?: arrayOf()
        val optimalSize = jpegSizes.maxByOrNull { it.width * it.height } ?: android.util.Size(1920, 1080)

        imageReader?.close()
        imageReader = ImageReader.newInstance(
            optimalSize.width,
            optimalSize.height,
            ImageFormat.JPEG,
            2
        )
    }

    private fun createCameraPreviewSession() {
        val device = cameraDevice ?: return
        val surface = previewSurface ?: return
        val reader = imageReader ?: return

        try {
            previewRequestBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                addTarget(surface)
            }

            val surfaces = listOf(surface, reader.surface)
            device.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureSession = session
                    applyCameraParameters()
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e("Camera2Engine", "Session configuration failed")
                }
            }, backgroundHandler)
        } catch (e: Exception) {
            Log.e("Camera2Engine", "Exception creating session", e)
        }
    }

    fun updateSettings(
        pasm: PasmMode,
        iso: Int,
        shutterSpeed: Double,
        ev: Int,
        focus: FocusMode,
        manualFocusDist: Float,
        flash: FlashMode,
        recipe: FilmRecipe
    ) {
        this.pasmMode = pasm
        this.manualIso = iso
        this.manualShutterSeconds = shutterSpeed
        this.evCompensation = ev
        this.focusMode = focus
        this.manualFocusDistance = manualFocusDist
        this.flashMode = flash
        this.activeRecipe = recipe

        applyCameraParameters()
    }

    private fun applyCameraParameters() {
        val session = captureSession ?: return
        val builder = previewRequestBuilder ?: return

        try {
            // Apply PASM controls
            when (pasmMode) {
                PasmMode.PROGRAM, PasmMode.APERTURE -> {
                    builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                    builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, evCompensation)
                }
                PasmMode.SHUTTER, PasmMode.MANUAL -> {
                    builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                    val exposureTimeNanos = (manualShutterSeconds * 1_000_000_000L).toLong()
                    builder.set(CaptureRequest.SENSOR_EXPOSURE_TIME, exposureTimeNanos)
                    builder.set(CaptureRequest.SENSOR_SENSITIVITY, manualIso)
                }
            }

            // Apply Focus controls
            when (focusMode) {
                FocusMode.AF_CONTINUOUS -> {
                    builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                }
                FocusMode.AF_SINGLE -> {
                    builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
                }
                FocusMode.MANUAL_FOCUS -> {
                    builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
                    builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, manualFocusDistance)
                }
            }

            // Flash mode
            when (flashMode) {
                FlashMode.OFF -> {
                    builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF)
                }
                FlashMode.AUTO -> {
                    builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                }
                FlashMode.ON -> {
                    builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH)
                }
                FlashMode.TORCH -> {
                    builder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH)
                }
            }

            session.setRepeatingRequest(builder.build(), captureCallback, backgroundHandler)
        } catch (e: Exception) {
            Log.e("Camera2Engine", "Failed to apply camera parameters", e)
        }
    }

    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            val iso = result.get(CaptureResult.SENSOR_SENSITIVITY) ?: manualIso
            val expNanos = result.get(CaptureResult.SENSOR_EXPOSURE_TIME)
            val aperture = result.get(CaptureResult.LENS_APERTURE) ?: 1.8f
            val afState = result.get(CaptureResult.CONTROL_AF_STATE)

            _currentIso.value = iso
            if (expNanos != null && expNanos > 0) {
                _currentShutterSpeed.value = expNanos.toDouble() / 1_000_000_000.0
            }
            _currentAperture.value = aperture
            _afLocked.value = afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                    afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED

            // Synthesize live histogram data based on ISO & exposure
            updateHistogram(iso, _currentShutterSpeed.value)
        }
    }

    private var histogramCounter = 0
    private fun updateHistogram(iso: Int, shutterSec: Double) {
        histogramCounter++
        if (histogramCounter % 3 != 0) return

        val bins = FloatArray(32)
        val normalizedLight = min(1.0, max(0.05, (iso / 400.0) * (shutterSec * 125.0)))
        val peakIndex = (normalizedLight * 28).toInt().coerceIn(2, 29)

        for (i in 0 until 32) {
            val dist = (i - peakIndex).toFloat()
            val value = (1f / (1f + dist * dist * 0.35f)) * (0.4f + (i % 3) * 0.15f)
            bins[i] = value.coerceIn(0.05f, 0.95f)
        }
        _histogramData.value = bins
    }

    fun triggerTapFocus(xNorm: Float, yNorm: Float) {
        val session = captureSession ?: return
        val builder = previewRequestBuilder ?: return
        val characteristics = cameraCharacteristics ?: return
        val sensorRect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE) ?: return

        val halfArea = 150
        val centerX = (xNorm * sensorRect.width()).toInt().coerceIn(halfArea, sensorRect.width() - halfArea)
        val centerY = (yNorm * sensorRect.height()).toInt().coerceIn(halfArea, sensorRect.height() - halfArea)

        val meteringRect = MeteringRectangle(
            centerX - halfArea,
            centerY - halfArea,
            halfArea * 2,
            halfArea * 2,
            MeteringRectangle.METERING_WEIGHT_MAX
        )

        try {
            builder.set(CaptureRequest.CONTROL_AF_REGIONS, arrayOf(meteringRect))
            builder.set(CaptureRequest.CONTROL_AE_REGIONS, arrayOf(meteringRect))
            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START)
            session.capture(builder.build(), null, backgroundHandler)
            builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE)
            session.setRepeatingRequest(builder.build(), captureCallback, backgroundHandler)
        } catch (e: Exception) {
            Log.e("Camera2Engine", "Tap to focus error", e)
        }
    }

    fun capturePhoto(
        recipe: FilmRecipe = activeRecipe,
        onPhotoCaptured: (CapturedPhoto) -> Unit
    ) {
        val device = cameraDevice ?: return
        val session = captureSession ?: return
        val reader = imageReader ?: return

        reader.setOnImageAvailableListener({ imageReader ->
            val image = imageReader.acquireLatestImage() ?: return@setOnImageAvailableListener
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            image.close()

            backgroundHandler?.post {
                val savedPhoto = processAndSavePhoto(bytes, recipe)
                if (savedPhoto != null) {
                    onPhotoCaptured(savedPhoto)
                }
            }
        }, backgroundHandler)

        try {
            val captureBuilder = device.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE).apply {
                addTarget(reader.surface)
                set(CaptureRequest.JPEG_ORIENTATION, 90)

                when (pasmMode) {
                    PasmMode.PROGRAM, PasmMode.APERTURE -> {
                        set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                        set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, evCompensation)
                    }
                    PasmMode.SHUTTER, PasmMode.MANUAL -> {
                        set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF)
                        val expNanos = (manualShutterSeconds * 1_000_000_000L).toLong()
                        set(CaptureRequest.SENSOR_EXPOSURE_TIME, expNanos)
                        set(CaptureRequest.SENSOR_SENSITIVITY, manualIso)
                    }
                }
            }

            session.capture(captureBuilder.build(), null, backgroundHandler)
        } catch (e: Exception) {
            Log.e("Camera2Engine", "Capture still photo error", e)
        }
    }

    private fun processAndSavePhoto(rawJpegBytes: ByteArray, recipe: FilmRecipe): CapturedPhoto? {
        val originalBitmap = BitmapFactory.decodeByteArray(rawJpegBytes, 0, rawJpegBytes.size) ?: return null
        val processedBitmap = applyFilmRecipe(originalBitmap, recipe)

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "PHOTON_${recipe.id.uppercase()}_$timeStamp.jpg"

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM + "/PhotonCamera")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null

        try {
            resolver.openOutputStream(uri)?.use { output ->
                processedBitmap.compress(Bitmap.CompressFormat.JPEG, 96, output)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            }

            // Write EXIF attributes
            context.contentResolver.openFileDescriptor(uri, "rw")?.use { pfd ->
                val exif = ExifInterface(pfd.fileDescriptor)
                exif.setAttribute(ExifInterface.TAG_MAKE, "PhotonCamera")
                exif.setAttribute(ExifInterface.TAG_MODEL, "Photon Mirrorless")
                exif.setAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY, _currentIso.value.toString())
                exif.setAttribute(ExifInterface.TAG_F_NUMBER, String.format(Locale.US, "%.1f", _currentAperture.value))
                exif.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, String.format(Locale.US, "%.5f", _currentShutterSpeed.value))
                exif.setAttribute(ExifInterface.TAG_SOFTWARE, "Photon Camera Film Engine - ${recipe.name}")
                exif.saveAttributes()
            }

            return CapturedPhoto(
                uri = uri,
                filePath = fileName,
                timestamp = System.currentTimeMillis(),
                width = processedBitmap.width,
                height = processedBitmap.height,
                iso = _currentIso.value,
                shutterSpeedSeconds = _currentShutterSpeed.value,
                aperture = _currentAperture.value,
                evBias = evCompensation * 0.33f,
                focalLengthMm = if (currentLens == CameraLens.ULTRA_WIDE) 14f else if (currentLens == CameraLens.TELEPHOTO) 50f else 26f,
                recipeName = recipe.name
            )
        } catch (e: Exception) {
            Log.e("Camera2Engine", "Failed to save photo", e)
            return null
        } finally {
            if (originalBitmap != processedBitmap) {
                originalBitmap.recycle()
            }
            processedBitmap.recycle()
        }
    }

    private fun applyFilmRecipe(src: Bitmap, recipe: FilmRecipe): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Color matrix for contrast & saturation
        val cm = ColorMatrix()
        cm.setSaturation(recipe.saturation)

        // Contrast adjustment
        val scale = recipe.contrast
        val translate = (-0.5f * scale + 0.5f) * 255f
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(contrastMatrix)

        // Kelvin Temperature & Tint adjustment
        val tempFactor = (recipe.colorTempK - 5500) / 4500f
        val rShift = tempFactor * 25f
        val bShift = -tempFactor * 25f
        val gShift = recipe.tint * 0.8f
        val colorBalanceMatrix = ColorMatrix(
            floatArrayOf(
                1f, 0f, 0f, 0f, rShift,
                0f, 1f, 0f, 0f, gShift,
                0f, 0f, 1f, 0f, bShift,
                0f, 0f, 0f, 1f, 0f
            )
        )
        cm.postConcat(colorBalanceMatrix)

        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)

        // Lens Vignette
        if (recipe.vignette > 0.05f) {
            val cx = src.width / 2f
            val cy = src.height / 2f
            val radius = max(cx, cy) * 1.25f
            val vignettePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                val alpha = (recipe.vignette * 180).toInt().coerceIn(0, 255)
                shader = RadialGradient(
                    cx, cy, radius,
                    intArrayOf(0x00000000, 0x00000000, (alpha shl 24) or 0x000000),
                    floatArrayOf(0.0f, 0.5f, 1.0f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, src.width.toFloat(), src.height.toFloat(), vignettePaint)
        }

        // Film Grain Texture overlay
        if (recipe.grain > 0.05f) {
            val grainPaint = Paint().apply {
                color = 0xFFFFFFFF.toInt()
                alpha = (recipe.grain * 35).toInt().coerceIn(0, 50)
            }
            val random = Random(42)
            for (i in 0 until (src.width * src.height / 800)) {
                val gx = random.nextFloat() * src.width
                val gy = random.nextFloat() * src.height
                canvas.drawPoint(gx, gy, grainPaint)
            }
        }

        return out
    }

    private fun closeCamera() {
        captureSession?.close()
        captureSession = null
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null
        _isCameraReady.value = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        val x = event.values[0]
        val y = event.values[1]
        // Calculate roll in degrees
        val roll = Math.toDegrees(atan2(x.toDouble(), y.toDouble())).toFloat()
        _horizonRollAngle.value = roll
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
