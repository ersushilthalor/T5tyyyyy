package com.hinnka.mycamera.model

import androidx.annotation.StringRes
import com.hinnka.mycamera.R

enum class CameraLens(
    val factor: Float,
    val displayLabel: String,
    @get:StringRes val titleRes: Int
) {
    ULTRA_WIDE(0.6f, "0.6x", R.string.lens_ultra_wide),
    WIDE(1.0f, "1.0x", R.string.lens_wide),
    TELEPHOTO(2.0f, "2.0x", R.string.lens_telephoto),
    FRONT(1.0f, "FRONT", R.string.lens_front)
}

enum class AspectRatio(
    val ratio: Float,
    @get:StringRes val labelRes: Int
) {
    RATIO_4_3(4f / 3f, R.string.ratio_4_3),
    RATIO_16_9(16f / 9f, R.string.ratio_16_9),
    RATIO_1_1(1f, R.string.ratio_1_1),
    RATIO_3_2(3f / 2f, R.string.ratio_3_2)
}

enum class GridType(
    @get:StringRes val labelRes: Int
) {
    NONE(R.string.grid_off),
    RULE_OF_THIRDS(R.string.grid_rule_of_thirds),
    GOLDEN_RATIO(R.string.grid_golden_ratio)
}

enum class FlashMode(
    @get:StringRes val labelRes: Int
) {
    OFF(R.string.flash_off),
    AUTO(R.string.flash_auto),
    ON(R.string.flash_on),
    TORCH(R.string.torch_on)
}
