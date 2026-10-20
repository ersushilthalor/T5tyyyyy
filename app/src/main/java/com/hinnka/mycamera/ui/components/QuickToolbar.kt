package com.hinnka.mycamera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hinnka.mycamera.R
import com.hinnka.mycamera.model.AspectRatio
import com.hinnka.mycamera.model.FlashMode
import com.hinnka.mycamera.model.GridType
import com.hinnka.mycamera.ui.theme.CameraAccentAmber
import com.hinnka.mycamera.ui.theme.CameraAfGreen
import com.hinnka.mycamera.ui.theme.CameraBodyDark
import com.hinnka.mycamera.ui.theme.CameraBodySurface
import com.hinnka.mycamera.ui.theme.CameraOsdGray
import com.hinnka.mycamera.ui.theme.CameraOsdWhite

@Composable
fun QuickToolbar(
    flashMode: FlashMode,
    onToggleFlash: () -> Unit,
    aspectRatio: AspectRatio,
    onToggleRatio: () -> Unit,
    gridType: GridType,
    onToggleGrid: () -> Unit,
    showLevel: Boolean,
    onToggleLevel: () -> Unit,
    showHistogram: Boolean,
    onToggleHistogram: () -> Unit,
    showZebra: Boolean,
    onToggleZebra: () -> Unit,
    showPeaking: Boolean,
    onTogglePeaking: () -> Unit,
    timerSeconds: Int,
    onCycleTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(CameraBodyDark.copy(alpha = 0.85f))
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Flash Button
        ToolbarButton(
            label = when (flashMode) {
                FlashMode.OFF -> "FLASH ✕"
                FlashMode.AUTO -> "FLASH A"
                FlashMode.ON -> "FLASH ⚡"
                FlashMode.TORCH -> "TORCH 💡"
            },
            isActive = flashMode != FlashMode.OFF,
            onClick = onToggleFlash,
            tag = "toolbar_flash"
        )

        // Timer Button
        ToolbarButton(
            label = if (timerSeconds > 0) "TIMER ${timerSeconds}s" else "TIMER ✕",
            isActive = timerSeconds > 0,
            onClick = onCycleTimer,
            tag = "toolbar_timer"
        )

        // Aspect Ratio
        ToolbarButton(
            label = stringResource(aspectRatio.labelRes),
            isActive = true,
            onClick = onToggleRatio,
            tag = "toolbar_ratio"
        )

        // Grid Button
        ToolbarButton(
            label = when (gridType) {
                GridType.NONE -> "GRID ✕"
                GridType.RULE_OF_THIRDS -> "3×3"
                GridType.GOLDEN_RATIO -> "PHI"
            },
            isActive = gridType != GridType.NONE,
            onClick = onToggleGrid,
            tag = "toolbar_grid"
        )

        // Level Button
        ToolbarButton(
            label = "LEVEL",
            isActive = showLevel,
            onClick = onToggleLevel,
            tag = "toolbar_level"
        )

        // Histogram Button
        ToolbarButton(
            label = "HIST",
            isActive = showHistogram,
            onClick = onToggleHistogram,
            tag = "toolbar_histogram"
        )

        // Zebra Button
        ToolbarButton(
            label = "ZEBRA",
            isActive = showZebra,
            onClick = onToggleZebra,
            tag = "toolbar_zebra"
        )

        // Focus Peaking
        ToolbarButton(
            label = "PEAK",
            isActive = showPeaking,
            onClick = onTogglePeaking,
            tag = "toolbar_peaking"
        )
    }
}

@Composable
private fun ToolbarButton(
    label: String,
    isActive: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .height(30.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isActive) CameraAccentAmber.copy(alpha = 0.2f) else CameraBodySurface
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isActive) CameraAccentAmber else CameraOsdGray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
        )
    }
}
