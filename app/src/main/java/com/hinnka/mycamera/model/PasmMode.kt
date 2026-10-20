package com.hinnka.mycamera.model

import androidx.annotation.StringRes
import com.hinnka.mycamera.R

enum class PasmMode(
    val code: String,
    @get:StringRes val titleRes: Int,
    @get:StringRes val descRes: Int
) {
    PROGRAM("P", R.string.mode_program, R.string.mode_program_desc),
    APERTURE("A", R.string.mode_aperture, R.string.mode_aperture_desc),
    SHUTTER("S", R.string.mode_shutter, R.string.mode_shutter_desc),
    MANUAL("M", R.string.mode_manual, R.string.mode_manual_desc)
}
