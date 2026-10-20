package com.hinnka.mycamera.ui.components

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.hinnka.mycamera.model.AspectRatio
import com.hinnka.mycamera.model.GridType
import com.hinnka.mycamera.ui.theme.CameraAfGreen
import com.hinnka.mycamera.ui.theme.CameraBodyDark
import com.hinnka.mycamera.ui.theme.CameraOsdWhite
import com.hinnka.mycamera.ui.theme.CameraZebraWarning
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun CameraViewfinder(
    aspectRatio: AspectRatio,
    gridType: GridType,
    showLevel: Boolean,
    rollAngle: Float,
    showZebra: Boolean,
    showPeaking: Boolean,
    onSurfaceCreated: (android.view.Surface) -> Unit,
    onSurfaceDestroyed: () -> Unit,
    onTapFocus: (Float, Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var tapFocusPoint by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(tapFocusPoint) {
        if (tapFocusPoint != null) {
            delay(1500)
            tapFocusPoint = null
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CameraBodyDark)
            .testTag("camera_viewfinder_container"),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .aspectRatio(1f / aspectRatio.ratio, matchHeightConstraintsFirst = true)
                .clip(RoundedCornerShape(0.dp))
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val normX = offset.x / size.width
                        val normY = offset.y / size.height
                        tapFocusPoint = offset
                        onTapFocus(normX, normY)
                    }
                }
        ) {
            // Android Camera SurfaceView
            AndroidView(
                factory = { context ->
                    SurfaceView(context).apply {
                        holder.addCallback(object : SurfaceHolder.Callback {
                            override fun surfaceCreated(holder: SurfaceHolder) {
                                onSurfaceCreated(holder.surface)
                            }

                            override fun surfaceChanged(
                                holder: SurfaceHolder,
                                format: Int,
                                width: Int,
                                height: Int
                            ) {}

                            override fun surfaceDestroyed(holder: SurfaceHolder) {
                                onSurfaceDestroyed()
                            }
                        })
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Composition Grids
            if (gridType != GridType.NONE) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    when (gridType) {
                        GridType.RULE_OF_THIRDS -> {
                            val lineColor = Color(0x66FFFFFF)
                            drawLine(lineColor, Offset(w / 3f, 0f), Offset(w / 3f, h), 1f)
                            drawLine(lineColor, Offset(w * 2f / 3f, 0f), Offset(w * 2f / 3f, h), 1f)
                            drawLine(lineColor, Offset(0f, h / 3f), Offset(w, h / 3f), 1f)
                            drawLine(lineColor, Offset(0f, h * 2f / 3f), Offset(w, h * 2f / 3f), 1f)
                        }
                        GridType.GOLDEN_RATIO -> {
                            val lineColor = Color(0x66F0A925)
                            val gx1 = w * 0.382f
                            val gx2 = w * 0.618f
                            val gy1 = h * 0.382f
                            val gy2 = h * 0.618f
                            drawLine(lineColor, Offset(gx1, 0f), Offset(gx1, h), 1f)
                            drawLine(lineColor, Offset(gx2, 0f), Offset(gx2, h), 1f)
                            drawLine(lineColor, Offset(0f, gy1), Offset(w, gy1), 1f)
                            drawLine(lineColor, Offset(0f, gy2), Offset(w, gy2), 1f)
                        }
                        GridType.NONE -> {}
                    }
                }
            }

            // Zebra overexposure warning stripes overlay simulation
            if (showZebra) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val stripeWidth = 14f
                    val stripeColor = CameraZebraWarning.copy(alpha = 0.35f)
                    var x = -h
                    while (x < w + h) {
                        drawLine(
                            color = stripeColor,
                            start = Offset(x, 0f),
                            end = Offset(x + h, h),
                            strokeWidth = stripeWidth
                        )
                        x += stripeWidth * 3f
                    }
                }
            }

            // Focus peaking highlights simulation
            if (showPeaking) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val peakingColor = CameraAfGreen.copy(alpha = 0.7f)
                    // Draw micro peaking crosshairs at center subject
                    drawCircle(peakingColor, radius = 4f, center = Offset(w * 0.5f, h * 0.5f))
                    drawCircle(peakingColor, radius = 3f, center = Offset(w * 0.45f, h * 0.48f))
                    drawCircle(peakingColor, radius = 3f, center = Offset(w * 0.55f, h * 0.52f))
                }
            }

            // Electronic Level Horizon Indicator
            if (showLevel) {
                val isLevel = abs(rollAngle) < 1.0f
                val levelColor = if (isLevel) CameraAfGreen else CameraOsdWhite.copy(alpha = 0.85f)

                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(160.dp, 40.dp)
                            .rotate(rollAngle)
                    ) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f

                        // Left horizon bar
                        drawLine(
                            color = levelColor,
                            start = Offset(cx - 70f, cy),
                            end = Offset(cx - 20f, cy),
                            strokeWidth = 2.5f
                        )
                        // Right horizon bar
                        drawLine(
                            color = levelColor,
                            start = Offset(cx + 20f, cy),
                            end = Offset(cx + 70f, cy),
                            strokeWidth = 2.5f
                        )
                        // Center reference circle
                        drawCircle(
                            color = levelColor,
                            radius = 4f,
                            center = Offset(cx, cy)
                        )
                    }

                    // Numeric degree readout below
                    Text(
                        text = "${String.format("%.1f", rollAngle)}°",
                        color = levelColor,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.offset(y = 28.dp)
                    )
                }
            }

            // Tap-to-focus indicator box
            tapFocusPoint?.let { point ->
                Box(
                    modifier = Modifier
                        .offset { IntOffset(point.x.toInt() - 32, point.y.toInt() - 32) }
                        .size(64.dp)
                        .border(1.5.dp, CameraAfGreen, RoundedCornerShape(4.dp))
                )
            }
        }
    }
}
