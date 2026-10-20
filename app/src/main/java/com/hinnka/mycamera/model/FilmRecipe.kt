package com.hinnka.mycamera.model

data class FilmRecipe(
    val id: String,
    val name: String,
    val isCustom: Boolean = false,
    val exposureBias: Float = 0.0f,
    val contrast: Float = 1.0f,
    val highlights: Float = 0.0f,
    val shadows: Float = 0.0f,
    val saturation: Float = 1.0f,
    val colorTempK: Int = 5500,
    val tint: Float = 0.0f,
    val vignette: Float = 0.0f,
    val grain: Float = 0.0f,
    val hdfDiffusion: Float = 0.0f,
    val colorFilterHex: Long = 0x00000000
) {
    companion object {
        val ClassicChrome = FilmRecipe(
            id = "classic_chrome",
            name = "Classic Chrome",
            contrast = 1.25f,
            highlights = -0.3f,
            shadows = 0.4f,
            saturation = 0.85f,
            colorTempK = 5200,
            tint = -4f,
            vignette = 0.15f,
            grain = 0.2f,
            colorFilterHex = 0x1A20354A
        )

        val Provia = FilmRecipe(
            id = "provia",
            name = "Provia Standard",
            contrast = 1.0f,
            highlights = 0.0f,
            shadows = 0.0f,
            saturation = 1.0f,
            colorTempK = 5500,
            tint = 0.0f,
            vignette = 0.0f,
            grain = 0.05f
        )

        val Velvia = FilmRecipe(
            id = "velvia",
            name = "Velvia Vivid",
            contrast = 1.35f,
            highlights = 0.2f,
            shadows = -0.2f,
            saturation = 1.45f,
            colorTempK = 5800,
            tint = 2.0f,
            vignette = 0.2f,
            grain = 0.1f,
            colorFilterHex = 0x1A1B4332
        )

        val Acros = FilmRecipe(
            id = "acros",
            name = "Acros B&W",
            contrast = 1.4f,
            highlights = -0.1f,
            shadows = 0.3f,
            saturation = 0.0f,
            colorTempK = 5500,
            tint = 0.0f,
            vignette = 0.25f,
            grain = 0.45f
        )

        val BleachBypass = FilmRecipe(
            id = "bleach_bypass",
            name = "Bleach Bypass",
            contrast = 1.7f,
            highlights = 0.4f,
            shadows = 0.6f,
            saturation = 0.4f,
            colorTempK = 5000,
            tint = -10f,
            vignette = 0.35f,
            grain = 0.5f,
            colorFilterHex = 0x223A4042
        )

        val NostalgicNeg = FilmRecipe(
            id = "nostalgic_neg",
            name = "Nostalgic Negative",
            contrast = 0.95f,
            highlights = -0.4f,
            shadows = -0.3f,
            saturation = 1.1f,
            colorTempK = 6200,
            tint = 6f,
            vignette = 0.2f,
            grain = 0.3f,
            colorFilterHex = 0x22E07A5F
        )

        val TealOrange = FilmRecipe(
            id = "teal_orange",
            name = "Cinema Teal & Orange",
            contrast = 1.3f,
            highlights = 0.2f,
            shadows = 0.2f,
            saturation = 1.25f,
            colorTempK = 5600,
            tint = -8f,
            vignette = 0.25f,
            grain = 0.15f,
            colorFilterHex = 0x200077B6
        )

        val Warm35 = FilmRecipe(
            id = "warm_35",
            name = "Cinematic Warm 35",
            contrast = 1.1f,
            highlights = -0.2f,
            shadows = -0.2f,
            saturation = 1.15f,
            colorTempK = 6500,
            tint = 4f,
            vignette = 0.15f,
            grain = 0.35f,
            hdfDiffusion = 0.3f,
            colorFilterHex = 0x22F4A261
        )

        val BuiltInRecipes = listOf(
            ClassicChrome,
            Provia,
            Velvia,
            Acros,
            BleachBypass,
            NostalgicNeg,
            TealOrange,
            Warm35
        )
    }
}
