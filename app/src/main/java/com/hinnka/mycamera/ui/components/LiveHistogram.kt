package com.hinnka.mycamera.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.hinnka.mycamera.ui.theme.CameraAccentAmber
import com.hinnka.mycamera.ui.theme.CameraBodyDark

@Composable
fun LiveHistogram(
    histogramData: FloatArray,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(96.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(CameraBodyDark.copy(alpha = 0.75f))
            .border(1.dp, Color(0x55FFFFFF), RoundedCornerShape(6.dp))
            .testTag("live_histogram")
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            if (histogramData.isEmpty()) return@Canvas
            val w = size.width
            val h = size.height
            val step = w / (histogramData.size - 1)

            val path = Path().apply {
                moveTo(0f, h)
                for (i in histogramData.indices) {
                    val x = i * step
                    val y = h - (histogramData[i].coerceIn(0f, 1f) * h * 0.9f)
                    lineTo(x, y)
                }
                lineTo(w, h)
                close()
            }

            drawPath(
                path = path,
                color = CameraAccentAmber.copy(alpha = 0.45f)
            )

            // Draw line stroke
            for (i in 0 until histogramData.size - 1) {
                val x1 = i * step
                val y1 = h - (histogramData[i].coerceIn(0f, 1f) * h * 0.9f)
                val x2 = (i + 1) * step
                val y2 = h - (histogramData[i + 1].coerceIn(0f, 1f) * h * 0.9f)
                drawLine(
                    color = Color.White.copy(alpha = 0.85f),
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 1.5f
                )
            }
        }
    }
}
