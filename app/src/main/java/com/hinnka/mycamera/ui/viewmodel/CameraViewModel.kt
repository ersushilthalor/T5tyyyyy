package com.hinnka.mycamera.ui.viewmodel

import android.app.Application
import android.content.Context
import android.media.MediaActionSound
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.Surface
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hinnka.mycamera.camera.Camera2Engine
import com.hinnka.mycamera.model.AspectRatio
import com.hinnka.mycamera.model.CameraLens
import com.hinnka.mycamera.model.CapturedPhoto
import com.hinnka.mycamera.model.FilmRecipe
import com.hinnka.mycamera.model.FlashMode
import com.hinnka.mycamera.model.FocusMode
import com.hinnka.mycamera.model.GridType
import com.hinnka.mycamera.model.PasmMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CameraViewModel(application: Application) : AndroidViewModel(application) {

    val cameraEngine = Camera2Engine(application)

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private val shutterSound = MediaActionSound().apply {
        load(MediaActionSound.SHUTTER_CLICK)
    }

    // Camera Control State
    private val _pasmMode = MutableStateFlow(PasmMode.PROGRAM)
    val pasmMode: StateFlow<PasmMode> = _pasmMode.asStateFlow()

    private val _currentLens = MutableStateFlow(CameraLens.WIDE)
    val currentLens: StateFlow<CameraLens> = _currentLens.asStateFlow()

    private val _iso = MutableStateFlow(200)
    val iso: StateFlow<Int> = _iso.asStateFlow()

    private val _shutterSpeed = MutableStateFlow(1.0 / 125.0)
    val shutterSpeed: StateFlow<Double> = _shutterSpeed.asStateFlow()

    private val _evCompensation = MutableStateFlow(0) // in 1/3 EV steps: -6 to +6
    val evCompensation: StateFlow<Int> = _evCompensation.asStateFlow()

    private val _focusMode = MutableStateFlow(FocusMode.AF_CONTINUOUS)
    val focusMode: StateFlow<FocusMode> = _focusMode.asStateFlow()

    private val _manualFocusDistance = MutableStateFlow(0.0f) // 0.0 = inf, 10.0 = macro
    val manualFocusDistance: StateFlow<Float> = _manualFocusDistance.asStateFlow()

    private val _flashMode = MutableStateFlow(FlashMode.OFF)
    val flashMode: StateFlow<FlashMode> = _flashMode.asStateFlow()

    private val _aspectRatio = MutableStateFlow(AspectRatio.RATIO_4_3)
    val aspectRatio: StateFlow<AspectRatio> = _aspectRatio.asStateFlow()

    private val _gridType = MutableStateFlow(GridType.RULE_OF_THIRDS)
    val gridType: StateFlow<GridType> = _gridType.asStateFlow()

    private val _showLevel = MutableStateFlow(true)
    val showLevel: StateFlow<Boolean> = _showLevel.asStateFlow()

    private val _showHistogram = MutableStateFlow(true)
    val showHistogram: StateFlow<Boolean> = _showHistogram.asStateFlow()

    private val _showZebra = MutableStateFlow(false)
    val showZebra: StateFlow<Boolean> = _showZebra.asStateFlow()

    private val _showFocusPeaking = MutableStateFlow(false)
    val showFocusPeaking: StateFlow<Boolean> = _showFocusPeaking.asStateFlow()

    private val _timerSeconds = MutableStateFlow(0) // 0, 3, 10
    val timerSeconds: StateFlow<Int> = _timerSeconds.asStateFlow()

    private val _countdownRemaining = MutableStateFlow(0)
    val countdownRemaining: StateFlow<Int> = _countdownRemaining.asStateFlow()

    // Recipes
    private val _recipes = MutableStateFlow(FilmRecipe.BuiltInRecipes)
    val recipes: StateFlow<List<FilmRecipe>> = _recipes.asStateFlow()

    private val _activeRecipe = MutableStateFlow(FilmRecipe.ClassicChrome)
    val activeRecipe: StateFlow<FilmRecipe> = _activeRecipe.asStateFlow()

    // Photos
    private val _capturedPhotos = MutableStateFlow<List<CapturedPhoto>>(emptyList())
    val capturedPhotos: StateFlow<List<CapturedPhoto>> = _capturedPhotos.asStateFlow()

    private val _selectedPhoto = MutableStateFlow<CapturedPhoto?>(null)
    val selectedPhoto: StateFlow<CapturedPhoto?> = _selectedPhoto.asStateFlow()

    // Burst
    private val _isBurstActive = MutableStateFlow(false)
    val isBurstActive: StateFlow<Boolean> = _isBurstActive.asStateFlow()

    private val _burstCount = MutableStateFlow(0)
    val burstCount: StateFlow<Int> = _burstCount.asStateFlow()

    private var burstJob: Job? = null

    // Navigation state: "camera" or "gallery"
    private val _currentScreen = MutableStateFlow("camera")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    fun startCamera() {
        cameraEngine.start()
        cameraEngine.openCamera(_currentLens.value)
    }

    fun stopCamera() {
        cameraEngine.stop()
    }

    fun onSurfaceReady(surface: Surface) {
        cameraEngine.setPreviewSurface(surface)
        syncEngineSettings()
    }

    fun onSurfaceDestroyed() {
        cameraEngine.setPreviewSurface(null)
    }

    private fun syncEngineSettings() {
        cameraEngine.updateSettings(
            pasm = _pasmMode.value,
            iso = _iso.value,
            shutterSpeed = _shutterSpeed.value,
            ev = _evCompensation.value,
            focus = _focusMode.value,
            manualFocusDist = _manualFocusDistance.value,
            flash = _flashMode.value,
            recipe = _activeRecipe.value
        )
    }

    fun setPasmMode(mode: PasmMode) {
        _pasmMode.value = mode
        triggerHapticClick()
        syncEngineSettings()
    }

    fun setLens(lens: CameraLens) {
        if (_currentLens.value == lens) return
        _currentLens.value = lens
        triggerHapticClick()
        cameraEngine.openCamera(lens)
        syncEngineSettings()
    }

    fun setIso(value: Int) {
        _iso.value = value
        triggerHapticClick()
        syncEngineSettings()
    }

    fun setShutterSpeed(seconds: Double) {
        _shutterSpeed.value = seconds
        triggerHapticClick()
        syncEngineSettings()
    }

    fun setEvCompensation(ev: Int) {
        _evCompensation.value = ev
        triggerHapticClick()
        syncEngineSettings()
    }

    fun setFocusMode(mode: FocusMode) {
        _focusMode.value = mode
        triggerHapticClick()
        syncEngineSettings()
    }

    fun setManualFocusDistance(distance: Float) {
        _manualFocusDistance.value = distance
        syncEngineSettings()
    }

    fun toggleFlashMode() {
        val next = when (_flashMode.value) {
            FlashMode.OFF -> FlashMode.AUTO
            FlashMode.AUTO -> FlashMode.ON
            FlashMode.ON -> FlashMode.TORCH
            FlashMode.TORCH -> FlashMode.OFF
        }
        _flashMode.value = next
        triggerHapticClick()
        syncEngineSettings()
    }

    fun toggleAspectRatio() {
        val next = when (_aspectRatio.value) {
            AspectRatio.RATIO_4_3 -> AspectRatio.RATIO_16_9
            AspectRatio.RATIO_16_9 -> AspectRatio.RATIO_1_1
            AspectRatio.RATIO_1_1 -> AspectRatio.RATIO_3_2
            AspectRatio.RATIO_3_2 -> AspectRatio.RATIO_4_3
        }
        _aspectRatio.value = next
        triggerHapticClick()
    }

    fun toggleGrid() {
        val next = when (_gridType.value) {
            GridType.NONE -> GridType.RULE_OF_THIRDS
            GridType.RULE_OF_THIRDS -> GridType.GOLDEN_RATIO
            GridType.GOLDEN_RATIO -> GridType.NONE
        }
        _gridType.value = next
        triggerHapticClick()
    }

    fun toggleLevel() {
        _showLevel.value = !_showLevel.value
        triggerHapticClick()
    }

    fun toggleHistogram() {
        _showHistogram.value = !_showHistogram.value
        triggerHapticClick()
    }

    fun toggleZebra() {
        _showZebra.value = !_showZebra.value
        triggerHapticClick()
    }

    fun toggleFocusPeaking() {
        _showFocusPeaking.value = !_showFocusPeaking.value
        triggerHapticClick()
    }

    fun cycleTimer() {
        _timerSeconds.value = when (_timerSeconds.value) {
            0 -> 3
            3 -> 10
            else -> 0
        }
        triggerHapticClick()
    }

    fun selectRecipe(recipe: FilmRecipe) {
        _activeRecipe.value = recipe
        triggerHapticClick()
        syncEngineSettings()
    }

    fun saveCustomRecipe(recipe: FilmRecipe) {
        val list = _recipes.value.toMutableList()
        val index = list.indexOfFirst { it.id == recipe.id }
        if (index >= 0) {
            list[index] = recipe
        } else {
            list.add(recipe)
        }
        _recipes.value = list
        _activeRecipe.value = recipe
        triggerHapticClick()
        syncEngineSettings()
    }

    fun triggerTapFocus(xNorm: Float, yNorm: Float) {
        triggerHapticLight()
        cameraEngine.triggerTapFocus(xNorm, yNorm)
    }

    fun onShutterPress() {
        if (_countdownRemaining.value > 0) return

        val timer = _timerSeconds.value
        if (timer > 0) {
            viewModelScope.launch {
                for (t in timer downTo 1) {
                    _countdownRemaining.value = t
                    triggerHapticLight()
                    delay(1000)
                }
                _countdownRemaining.value = 0
                executeCapture()
            }
        } else {
            executeCapture()
        }
    }

    fun startBurstShooting() {
        if (_isBurstActive.value) return
        _isBurstActive.value = true
        _burstCount.value = 0

        burstJob = viewModelScope.launch {
            while (_isBurstActive.value) {
                _burstCount.value += 1
                executeCapture()
                delay(180)
            }
        }
    }

    fun stopBurstShooting() {
        _isBurstActive.value = false
        burstJob?.cancel()
        burstJob = null
    }

    private fun executeCapture() {
        triggerHapticShutter()
        shutterSound.play(MediaActionSound.SHUTTER_CLICK)
        cameraEngine.capturePhoto(_activeRecipe.value) { photo ->
            val updated = listOf(photo) + _capturedPhotos.value
            _capturedPhotos.value = updated
        }
    }

    fun openGallery(photo: CapturedPhoto? = _capturedPhotos.value.firstOrNull()) {
        _selectedPhoto.value = photo
        _currentScreen.value = "gallery"
    }

    fun closeGallery() {
        _currentScreen.value = "camera"
    }

    fun deleteSelectedPhoto() {
        val current = _selectedPhoto.value ?: return
        val list = _capturedPhotos.value.filter { it.uri != current.uri }
        _capturedPhotos.value = list
        _selectedPhoto.value = list.firstOrNull()
        if (list.isEmpty()) {
            _currentScreen.value = "camera"
        }
    }

    fun triggerHapticClick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(15)
        }
    }

    private fun triggerHapticLight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(10)
        }
    }

    private fun triggerHapticShutter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(35)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopCamera()
        shutterSound.release()
    }
}
