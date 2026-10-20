package com.hinnka.mycamera.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hinnka.mycamera.ui.theme.CameraAccentAmber
import com.hinnka.mycamera.ui.theme.CameraBodyDark
import com.hinnka.mycamera.ui.theme.CameraDialGroove
import com.hinnka.mycamera.ui.theme.CameraDialMetallic

@Composable
fun TwoStageShutterButton(
    isBurstActive: Boolean,
    burstCount: Int,
    countdownRemaining: Int,
    onSingleShot: () -> Unit,
    onStartBurst: () -> Unit,
    onStopBurst: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed || isBurstActive) 0.90f else 1.0f,
        label = "shutter_scale"
    )

    Box(
        modifier = modifier
            .size(76.dp)
            .scale(scale)
            .shadow(8.dp, CircleShape)
            .clip(CircleShape)
            // Outer knurled metallic collar
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        CameraDialMetallic,
                        CameraDialGroove,
                        Color(0xFF101014)
                    )
                )
            )
            .border(2.5.dp, Color(0xFF555560), CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                        if (isBurstActive) {
                            onStopBurst()
                        }
                    },
                    onTap = {
                        onSingleShot()
                    },
                    onLongPress = {
                        onStartBurst()
                    }
                )
            }
            .testTag("shutter_button"),
        contentAlignment = Alignment.Center
    ) {
        // Inner button core
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    brush = if (isBurstActive) {
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFF453A),
                                Color(0xFFC82318)
                            )
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(
                                CameraAccentAmber,
                                Color(0xFFE08E1A),
                                Color(0xFF9E5C08)
                            )
                        )
                    }
                )
                .border(1.5.dp, Color(0xFFFFFFFF).copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (countdownRemaining > 0) {
                Text(
                    text = "$countdownRemaining",
                    color = CameraBodyDark,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                )
            } else if (isBurstActive) {
                Text(
                    text = "$burstCount",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}
