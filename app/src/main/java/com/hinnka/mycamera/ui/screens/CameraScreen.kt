package com.hinnka.mycamera.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hinnka.mycamera.R
import com.hinnka.mycamera.model.CameraLens
import com.hinnka.mycamera.model.FocusMode
import com.hinnka.mycamera.model.PasmMode
import com.hinnka.mycamera.ui.components.CameraViewfinder
import com.hinnka.mycamera.ui.components.CameraWheelControl
import com.hinnka.mycamera.ui.components.LiveHistogram
import com.hinnka.mycamera.ui.components.PasmDial
import com.hinnka.mycamera.ui.components.QuickToolbar
import com.hinnka.mycamera.ui.components.RecipeDrawer
import com.hinnka.mycamera.ui.components.RecipeEditorDialog
import com.hinnka.mycamera.ui.components.TwoStageShutterButton
import com.hinnka.mycamera.ui.components.ViewfinderOsd
import com.hinnka.mycamera.ui.theme.CameraAccentAmber
import com.hinnka.mycamera.ui.theme.CameraBodyDark
import com.hinnka.mycamera.ui.theme.CameraBodySurface
import com.hinnka.mycamera.ui.theme.CameraOsdGray
import com.hinnka.mycamera.ui.theme.CameraOsdWhite
import com.hinnka.mycamera.ui.viewmodel.CameraViewModel

@Composable
fun CameraScreen(
    viewModel: CameraViewModel,
    modifier: Modifier = Modifier
) {
    val pasmMode by viewModel.pasmMode.collectAsState()
    val currentLens by viewModel.currentLens.collectAsState()
    val iso by viewModel.iso.collectAsState()
    val shutterSpeed by viewModel.shutterSpeed.collectAsState()
    val evCompensation by viewModel.evCompensation.collectAsState()
    val focusMode by viewModel.focusMode.collectAsState()
    val manualFocusDistance by viewModel.manualFocusDistance.collectAsState()
    val flashMode by viewModel.flashMode.collectAsState()
    val aspectRatio by viewModel.aspectRatio.collectAsState()
    val gridType by viewModel.gridType.collectAsState()
    val showLevel by viewModel.showLevel.collectAsState()
    val showHistogram by viewModel.showHistogram.collectAsState()
    val showZebra by viewModel.showZebra.collectAsState()
    val showPeaking by viewModel.showFocusPeaking.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val countdownRemaining by viewModel.countdownRemaining.collectAsState()
    val recipes by viewModel.recipes.collectAsState()
    val activeRecipe by viewModel.activeRecipe.collectAsState()
    val capturedPhotos by viewModel.capturedPhotos.collectAsState()
    val isBurstActive by viewModel.isBurstActive.collectAsState()
    val burstCount by viewModel.burstCount.collectAsState()

    // Engine telemetry
    val currentIso by viewModel.cameraEngine.currentIso.collectAsState()
    val currentShutter by viewModel.cameraEngine.currentShutterSpeed.collectAsState()
    val currentAperture by viewModel.cameraEngine.currentAperture.collectAsState()
    val afLocked by viewModel.cameraEngine.afLocked.collectAsState()
    val histogramData by viewModel.cameraEngine.histogramData.collectAsState()
    val horizonRoll by viewModel.cameraEngine.horizonRollAngle.collectAsState()

    var showRecipeEditor by remember { mutableStateOf(false) }
    var activeWheelTab by remember { mutableStateOf("EV") }

    // Predefined values for jog wheels
    val shutterSpeeds = listOf(
        1.0 / 4000.0, 1.0 / 2000.0, 1.0 / 1000.0, 1.0 / 500.0,
        1.0 / 250.0, 1.0 / 125.0, 1.0 / 60.0, 1.0 / 30.0,
        1.0 / 15.0, 1.0 / 8.0, 1.0 / 4.0, 0.5, 1.0, 2.0
    )
    val isoValues = listOf(50, 100, 200, 400, 800, 1600, 3200, 6400)
    val evSteps = (-6..6).toList()
    val focusDistances = listOf(0.0f, 0.1f, 0.25f, 0.5f, 1.0f, 2.0f, 5.0f, 10.0f)

    Scaffold(
        containerColor = CameraBodyDark,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("camera_screen_root")
        ) {
            // 1. Top Quick Settings Toolbar
            QuickToolbar(
                flashMode = flashMode,
                onToggleFlash = { viewModel.toggleFlashMode() },
                aspectRatio = aspectRatio,
                onToggleRatio = { viewModel.toggleAspectRatio() },
                gridType = gridType,
                onToggleGrid = { viewModel.toggleGrid() },
                showLevel = showLevel,
                onToggleLevel = { viewModel.toggleLevel() },
                showHistogram = showHistogram,
                onToggleHistogram = { viewModel.toggleHistogram() },
                showZebra = showZebra,
                onToggleZebra = { viewModel.toggleZebra() },
                showPeaking = showPeaking,
                onTogglePeaking = { viewModel.toggleFocusPeaking() },
                timerSeconds = timerSeconds,
                onCycleTimer = { viewModel.cycleTimer() }
            )

            // 2. Viewfinder Area with OSD & Floating Elements
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Viewfinder Surface & Overlays
                CameraViewfinder(
                    aspectRatio = aspectRatio,
                    gridType = gridType,
                    showLevel = showLevel,
                    rollAngle = horizonRoll,
                    showZebra = showZebra,
                    showPeaking = showPeaking,
                    onSurfaceCreated = { surface -> viewModel.onSurfaceReady(surface) },
                    onSurfaceDestroyed = { viewModel.onSurfaceDestroyed() },
                    onTapFocus = { x, y -> viewModel.triggerTapFocus(x, y) },
                    modifier = Modifier.fillMaxSize()
                )

                // Top Floating Row: PASM dial on left, Live Histogram on right
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    PasmDial(
                        currentMode = pasmMode,
                        onModeSelected = { mode ->
                            viewModel.setPasmMode(mode)
                            activeWheelTab = when (mode) {
                                PasmMode.SHUTTER -> "SHUTTER"
                                PasmMode.MANUAL -> "ISO"
                                else -> "EV"
                            }
                        }
                    )

                    if (showHistogram) {
                        LiveHistogram(histogramData = histogramData)
                    }
                }

                // OSD Telemetry Bar (Aperture, Shutter, ISO, EV, Recipe)
                ViewfinderOsd(
                    pasmMode = pasmMode,
                    shutterSpeed = currentShutter,
                    iso = currentIso,
                    aperture = currentAperture,
                    ev = evCompensation,
                    recipe = activeRecipe,
                    focusMode = focusMode,
                    isAfLocked = afLocked,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            // 3. Middle Control Deck: Jog Wheel for active parameter
            when (activeWheelTab) {
                "SHUTTER" -> {
                    CameraWheelControl(
                        title = stringResource(R.string.shutter_speed),
                        items = shutterSpeeds,
                        selectedItem = shutterSpeed,
                        itemLabel = { s ->
                            if (s >= 1.0) String.format("%.1f\"", s) else "1/${(1.0 / s).toInt()}"
                        },
                        onItemSelected = { viewModel.setShutterSpeed(it) }
                    )
                }
                "ISO" -> {
                    CameraWheelControl(
                        title = stringResource(R.string.iso),
                        items = isoValues,
                        selectedItem = iso,
                        itemLabel = { "ISO $it" },
                        onItemSelected = { viewModel.setIso(it) }
                    )
                }
                "FOCUS" -> {
                    CameraWheelControl(
                        title = stringResource(R.string.focus),
                        items = focusDistances,
                        selectedItem = manualFocusDistance,
                        itemLabel = { d ->
                            if (d == 0.0f) "INF" else if (d >= 5.0f) "MACRO" else String.format("%.1fm", 1.0f / d)
                        },
                        onItemSelected = { viewModel.setManualFocusDistance(it) }
                    )
                }
                else -> {
                    CameraWheelControl(
                        title = stringResource(R.string.ev_comp),
                        items = evSteps,
                        selectedItem = evCompensation,
                        itemLabel = { step ->
                            val v = step * 0.33f
                            if (step > 0) "+${String.format("%.1f", v)}" else String.format("%.1f", v)
                        },
                        onItemSelected = { viewModel.setEvCompensation(it) }
                    )
                }
            }

            // 4. Parameter Selector Tabs (SHUTTER, ISO, EV, FOCUS)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CameraBodyDark)
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                listOf("EV", "SHUTTER", "ISO", "FOCUS").forEach { tab ->
                    val isSelected = activeWheelTab == tab
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable {
                                activeWheelTab = tab
                                viewModel.triggerHapticClick()
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) CameraAccentAmber else CameraOsdGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal
                        )
                    }
                }
            }

            // 5. Film Simulation Recipe Strip
            RecipeDrawer(
                recipes = recipes,
                activeRecipe = activeRecipe,
                onSelectRecipe = { viewModel.selectRecipe(it) },
                onOpenRecipeEditor = { showRecipeEditor = true }
            )

            // 6. Bottom Physical Camera Controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CameraBodyDark)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Gallery Thumbnail Review Button
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CameraBodySurface)
                        .border(1.5.dp, Color(0x44FFFFFF), RoundedCornerShape(10.dp))
                        .clickable { viewModel.openGallery() }
                        .testTag("gallery_thumbnail_button"),
                    contentAlignment = Alignment.Center
                ) {
                    val lastPhoto = capturedPhotos.firstOrNull()
                    if (lastPhoto != null) {
                        AsyncImage(
                            model = lastPhoto.uri,
                            contentDescription = "Review",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = "PLAY",
                            color = CameraOsdGray,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Lens Selector (0.6x, 1.0x, 2.0x, Front)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(CameraBodySurface)
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                ) {
                    CameraLens.entries.forEach { lens ->
                        val isSelected = currentLens == lens
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) CameraAccentAmber else Color.Transparent)
                                .clickable { viewModel.setLens(lens) }
                                .testTag("lens_button_${lens.displayLabel.lowercase()}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lens.displayLabel,
                                color = if (isSelected) CameraBodyDark else CameraOsdWhite,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Two-Stage Shutter Button with long-press burst
                TwoStageShutterButton(
                    isBurstActive = isBurstActive,
                    burstCount = burstCount,
                    countdownRemaining = countdownRemaining,
                    onSingleShot = { viewModel.onShutterPress() },
                    onStartBurst = { viewModel.startBurstShooting() },
                    onStopBurst = { viewModel.stopBurstShooting() }
                )

                // AF/MF Focus Mode Switch
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(CameraBodySurface)
                        .border(1.5.dp, Color(0x44FFFFFF), CircleShape)
                        .clickable {
                            val nextMode = when (focusMode) {
                                FocusMode.AF_CONTINUOUS -> FocusMode.AF_SINGLE
                                FocusMode.AF_SINGLE -> FocusMode.MANUAL_FOCUS
                                FocusMode.MANUAL_FOCUS -> FocusMode.AF_CONTINUOUS
                            }
                            viewModel.setFocusMode(nextMode)
                            if (nextMode == FocusMode.MANUAL_FOCUS) {
                                activeWheelTab = "FOCUS"
                            }
                        }
                        .testTag("focus_mode_toggle"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = focusMode.code,
                        color = CameraAccentAmber,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Dialog for Custom Recipe Creation & Tuning
        if (showRecipeEditor) {
            RecipeEditorDialog(
                initialRecipe = activeRecipe,
                onSave = { newRecipe ->
                    viewModel.saveCustomRecipe(newRecipe)
                    showRecipeEditor = false
                },
                onDismiss = { showRecipeEditor = false }
            )
        }
    }
}
