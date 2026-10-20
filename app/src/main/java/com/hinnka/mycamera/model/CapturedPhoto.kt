package com.hinnka.mycamera.model

import android.net.Uri

data class CapturedPhoto(
    val uri: Uri,
    val filePath: String,
    val timestamp: Long,
    val width: Int,
    val height: Int,
    val iso: Int = 100,
    val shutterSpeedSeconds: Double = 1.0 / 125.0,
    val aperture: Float = 1.8f,
    val evBias: Float = 0.0f,
    val focalLengthMm: Float = 26f,
    val recipeName: String = "Provia",
    val isRaw: Boolean = false
) {
    val formattedShutterSpeed: String
        get() = if (shutterSpeedSeconds >= 1.0) {
            String.format("%.1fs", shutterSpeedSeconds)
        } else {
            val denominator = (1.0 / shutterSpeedSeconds).toInt()
            "1/${denominator}s"
        }
}
