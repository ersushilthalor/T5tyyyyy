package com.hinnka.mycamera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.hinnka.mycamera.R
import com.hinnka.mycamera.model.FilmRecipe
import com.hinnka.mycamera.ui.theme.CameraAccentAmber
import com.hinnka.mycamera.ui.theme.CameraBodyDark
import com.hinnka.mycamera.ui.theme.CameraBodySurface
import com.hinnka.mycamera.ui.theme.CameraOsdGray
import com.hinnka.mycamera.ui.theme.CameraOsdWhite

@Composable
fun RecipeEditorDialog(
    initialRecipe: FilmRecipe,
    onSave: (FilmRecipe) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("${initialRecipe.name} Custom") }
    var contrast by remember { mutableFloatStateOf(initialRecipe.contrast) }
    var saturation by remember { mutableFloatStateOf(initialRecipe.saturation) }
    var colorTempK by remember { mutableIntStateOf(initialRecipe.colorTempK) }
    var tint by remember { mutableFloatStateOf(initialRecipe.tint) }
    var highlights by remember { mutableFloatStateOf(initialRecipe.highlights) }
    var shadows by remember { mutableFloatStateOf(initialRecipe.shadows) }
    var vignette by remember { mutableFloatStateOf(initialRecipe.vignette) }
    var grain by remember { mutableFloatStateOf(initialRecipe.grain) }
    var hdfDiffusion by remember { mutableFloatStateOf(initialRecipe.hdfDiffusion) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("recipe_editor_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CameraBodySurface)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = stringResource(R.string.create_new_recipe),
                    color = CameraAccentAmber,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )

                // Recipe Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.recipe_name_prompt)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = CameraOsdWhite,
                        unfocusedTextColor = CameraOsdWhite,
                        focusedBorderColor = CameraAccentAmber,
                        unfocusedBorderColor = CameraOsdGray,
                        focusedLabelColor = CameraAccentAmber,
                        unfocusedLabelColor = CameraOsdGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Sliders
                ParameterSlider(
                    title = stringResource(R.string.param_contrast),
                    value = contrast,
                    valueRange = 0.5f..2.0f,
                    formattedValue = String.format("%.2f", contrast),
                    onValueChange = { contrast = it }
                )

                ParameterSlider(
                    title = stringResource(R.string.param_saturation),
                    value = saturation,
                    valueRange = 0.0f..2.0f,
                    formattedValue = String.format("%.2f", saturation),
                    onValueChange = { saturation = it }
                )

                ParameterSlider(
                    title = stringResource(R.string.param_temperature),
                    value = colorTempK.toFloat(),
                    valueRange = 2500f..10000f,
                    formattedValue = "${colorTempK}K",
                    onValueChange = { colorTempK = it.toInt() }
                )

                ParameterSlider(
                    title = stringResource(R.string.param_tint),
                    value = tint,
                    valueRange = -50f..50f,
                    formattedValue = String.format("%+.0f", tint),
                    onValueChange = { tint = it }
                )

                ParameterSlider(
                    title = stringResource(R.string.param_highlights),
                    value = highlights,
                    valueRange = -2.0f..2.0f,
                    formattedValue = String.format("%+.1f", highlights),
                    onValueChange = { highlights = it }
                )

                ParameterSlider(
                    title = stringResource(R.string.param_shadows),
                    value = shadows,
                    valueRange = -2.0f..2.0f,
                    formattedValue = String.format("%+.1f", shadows),
                    onValueChange = { shadows = it }
                )

                ParameterSlider(
                    title = stringResource(R.string.param_vignette),
                    value = vignette,
                    valueRange = 0.0f..1.0f,
                    formattedValue = String.format("%.2f", vignette),
                    onValueChange = { vignette = it }
                )

                ParameterSlider(
                    title = stringResource(R.string.param_grain),
                    value = grain,
                    valueRange = 0.0f..1.0f,
                    formattedValue = String.format("%.2f", grain),
                    onValueChange = { grain = it }
                )

                ParameterSlider(
                    title = stringResource(R.string.param_hdf),
                    value = hdfDiffusion,
                    valueRange = 0.0f..1.0f,
                    formattedValue = String.format("%.2f", hdfDiffusion),
                    onValueChange = { hdfDiffusion = it }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = CameraBodyDark),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "CANCEL", color = CameraOsdGray)
                    }

                    Button(
                        onClick = {
                            val newRecipe = FilmRecipe(
                                id = "custom_${System.currentTimeMillis()}",
                                name = name.ifBlank { "Custom Recipe" },
                                isCustom = true,
                                contrast = contrast,
                                saturation = saturation,
                                colorTempK = colorTempK,
                                tint = tint,
                                highlights = highlights,
                                shadows = shadows,
                                vignette = vignette,
                                grain = grain,
                                hdfDiffusion = hdfDiffusion
                            )
                            onSave(newRecipe)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CameraAccentAmber),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("button_save_recipe")
                    ) {
                        Text(
                            text = stringResource(R.string.save_recipe).uppercase(),
                            color = CameraBodyDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParameterSlider(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    formattedValue: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                color = CameraOsdWhite,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = formattedValue,
                color = CameraAccentAmber,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = CameraAccentAmber,
                activeTrackColor = CameraAccentAmber,
                inactiveTrackColor = CameraBodyDark
            )
        )
    }
}
