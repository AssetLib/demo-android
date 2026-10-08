package com.assetlib.demo

import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

/** A normal Compose Painter over the SDK's verified raster or the app's bundled resource. No SVG renderer. */
@Composable
fun rememberAssetArtworkPainter(bitmap: Bitmap?, fallbackResource: Int): Painter {
    val verified = remember(bitmap) { bitmap?.let { BitmapPainter(it.asImageBitmap()) } }
    return verified ?: painterResource(fallbackResource)
}
