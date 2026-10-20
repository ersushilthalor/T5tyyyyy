package com.hinnka.mycamera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hinnka.mycamera.model.PasmMode
import com.hinnka.mycamera.ui.theme.CameraAccentAmber
import com.hinnka.mycamera.ui.theme.CameraBodyDark
import com.hinnka.mycamera.ui.theme.CameraDialGroove
import com.hinnka.mycamera.ui.theme.CameraDialMetallic
import com.hinnka.mycamera.ui.theme.CameraOsdGray
import com.hinnka.mycamera.ui.theme.CameraOsdWhite

@Composable
fun PasmDial(
    currentMode: PasmMode,
    onModeSelected: (PasmMode) -> Unit,
    modifier: Modifier = Modifier
) {
    // Knurled physical camera dial appearance
    Box(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CameraDialMetallic,
                        CameraDialGroove,
                        CameraDialMetallic
                    )
                )
            )
            .border(1.dp, Color(0xFF44444C), RoundedCornerShape(20.dp))
            .padding(horizontal = 4.dp, vertical = 3.dp)
            .testTag("pasm_dial_container")
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PasmMode.entries.forEach { mode ->
                val isSelected = mode == currentMode
                Box(
                    modifier = Modifier
                        .height(30.dp)
                        .width(36.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        CameraAccentAmber,
                                        Color(0xFFD48B1A)
                                    )
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0x33222226),
                                        Color(0x66111114)
                                    )
                                )
                            }
                        )
                        .clickable { onModeSelected(mode) }
                        .testTag("pasm_mode_${mode.code.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = mode.code,
                        color = if (isSelected) CameraBodyDark else CameraOsdWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
