package com.hinnka.mycamera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.hinnka.mycamera.model.FilmRecipe
import com.hinnka.mycamera.model.FocusMode
import com.hinnka.mycamera.model.PasmMode
import com.hinnka.mycamera.ui.theme.CameraAccentAmber
import com.hinnka.mycamera.ui.theme.CameraAfGreen
import com.hinnka.mycamera.ui.theme.CameraBodyDark
import com.hinnka.mycamera.ui.theme.CameraOsdGray
import com.hinnka.mycamera.ui.theme.CameraOsdWhite

@Composable
fun ViewfinderOsd(
    pasmMode: PasmMode,
    shutterSpeed: Double,
    iso: Int,
    aperture: Float,
    ev: Int,
    recipe: FilmRecipe,
    focusMode: FocusMode,
    isAfLocked: Boolean,
    modifier: Modifier = Modifier
) {
    val formattedShutter = if (shutterSpeed >= 1.0) {
        String.format("%.1f\"", shutterSpeed)
    } else {
        val denominator = (1.0 / shutterSpeed).toInt()
        "1/${denominator}"
    }

    val evText = when {
        ev > 0 -> "+${String.format("%.1f", ev * 0.33f)}"
        ev < 0 -> String.format("%.1f", ev * 0.33f)
        else -> "±0.0"
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("viewfinder_osd")
    ) {
        // Top status line: Battery, SD Space, RAW+JPG, Film Simulation badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                // Film Simulation Tag
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CameraAccentAmber.copy(alpha = 0.2f))
                        .border(1.dp, CameraAccentAmber, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = recipe.name.uppercase(),
                        color = CameraAccentAmber,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Format indicator
                Text(
                    text = stringResource(R.string.hud_raw_jpeg),
                    color = CameraOsdWhite.copy(alpha = 0.8f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.hud_card),
                    color = CameraOsdWhite.copy(alpha = 0.8f),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = stringResource(R.string.hud_battery),
                    color = CameraAfGreen,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom Telemetry Bar: Mode | Aperture | Shutter | ISO | EV | AF
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CameraBodyDark.copy(alpha = 0.65f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PASM Mode
                Text(
                    text = pasmMode.code,
                    color = CameraAccentAmber,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )

                // Aperture
                Text(
                    text = "F${String.format("%.1f", aperture)}",
                    color = CameraOsdWhite,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // Shutter
                Text(
                    text = formattedShutter,
                    color = CameraOsdWhite,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // ISO
                Text(
                    text = "ISO $iso",
                    color = CameraOsdWhite,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )

                // EV Compensation
                Text(
                    text = "EV $evText",
                    color = if (ev != 0) CameraAccentAmber else CameraOsdGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )

                // Focus Mode & AF lock status
                Text(
                    text = if (isAfLocked) stringResource(R.string.hud_af_locked) else focusMode.code,
                    color = if (isAfLocked) CameraAfGreen else CameraOsdGray,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
