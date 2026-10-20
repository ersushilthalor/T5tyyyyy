package com.hinnka.mycamera.model

import androidx.annotation.StringRes
import com.hinnka.mycamera.R

enum class FocusMode(
    val code: String,
    @get:StringRes val labelRes: Int
) {
    AF_CONTINUOUS("AF-C", R.string.af_continuous),
    AF_SINGLE("AF-S", R.string.af_single),
    MANUAL_FOCUS("MF", R.string.mf_manual)
}
