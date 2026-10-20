package com.hinnka.mycamera.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.hinnka.mycamera.R
import com.hinnka.mycamera.model.CapturedPhoto
import com.hinnka.mycamera.ui.theme.CameraAccentAmber
import com.hinnka.mycamera.ui.theme.CameraBodyDark
import com.hinnka.mycamera.ui.theme.CameraBodySurface
import com.hinnka.mycamera.ui.theme.CameraOsdGray
import com.hinnka.mycamera.ui.theme.CameraOsdWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GalleryScreen(
    photos: List<CapturedPhoto>,
    selectedPhoto: CapturedPhoto?,
    onSelectPhoto: (CapturedPhoto) -> Unit,
    onDeletePhoto: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    var showExifSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = CameraBodyDark,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("gallery_screen")
        ) {
            if (selectedPhoto == null || photos.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_photos_yet),
                        color = CameraOsdWhite,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.take_first_shot),
                        color = CameraOsdGray,
                        fontSize = 14.sp
                    )
                }
            } else {
                // Main Photo Display
                AsyncImage(
                    model = selectedPhoto.uri,
                    contentDescription = "Selected photo",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { showExifSheet = !showExifSheet }
                )

                // Top Toolbar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CameraBodyDark.copy(alpha = 0.7f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CameraBodySurface)
                            .clickable(onClick = onBack)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("gallery_back_button")
                    ) {
                        Text(
                            text = "◀ BACK",
                            color = CameraOsdWhite,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // EXIF Info toggle
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (showExifSheet) CameraAccentAmber else CameraBodySurface)
                                .clickable { showExifSheet = !showExifSheet }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("gallery_info_button")
                        ) {
                            Text(
                                text = "EXIF ℹ",
                                color = if (showExifSheet) CameraBodyDark else CameraOsdWhite,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Share button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CameraBodySurface)
                                .clickable {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/jpeg"
                                        putExtra(Intent.EXTRA_STREAM, selectedPhoto.uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Share Photo"))
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("gallery_share_button")
                        ) {
                            Text(
                                text = stringResource(R.string.share_photo).uppercase(),
                                color = CameraOsdWhite,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Delete button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF882222))
                                .clickable(onClick = onDeletePhoto)
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("gallery_delete_button")
                        ) {
                            Text(
                                text = stringResource(R.string.delete_photo).uppercase(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // EXIF Info Card Overlay
                if (showExifSheet) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(bottom = 76.dp, start = 12.dp, end = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = CameraBodySurface.copy(alpha = 0.95f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.exif_details).uppercase(),
                                color = CameraAccentAmber,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            ExifRow("Film Profile", selectedPhoto.recipeName)
                            ExifRow("Shutter Speed", selectedPhoto.formattedShutterSpeed)
                            ExifRow("Aperture", "F${String.format("%.1f", selectedPhoto.aperture)}")
                            ExifRow("ISO Sensitivity", "ISO ${selectedPhoto.iso}")
                            ExifRow("Focal Length", "${selectedPhoto.focalLengthMm.toInt()}mm eq.")
                            ExifRow("Dimensions", "${selectedPhoto.width} × ${selectedPhoto.height}")
                            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(selectedPhoto.timestamp))
                            ExifRow("Date / Time", dateStr)
                        }
                    }
                }

                // Bottom Thumbnail Film Strip
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(68.dp)
                        .background(CameraBodyDark.copy(alpha = 0.85f))
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    photos.forEach { photo ->
                        val isCurrent = photo.uri == selectedPhoto.uri
                        AsyncImage(
                            model = photo.uri,
                            contentDescription = "Thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) CameraAccentAmber else Color(0x33FFFFFF),
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable { onSelectPhoto(photo) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ExifRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = CameraOsdGray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = value,
            color = CameraOsdWhite,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold
        )
    }
}
