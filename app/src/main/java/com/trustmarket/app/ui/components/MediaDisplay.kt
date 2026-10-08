package com.trustmarket.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

private const val BASE_URL = "https://trustmarket.cc"

@Composable
fun MediaDisplay(
    mediaUrl: String?,
    mediaType: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val fullUrl = when {
        mediaUrl.isNullOrEmpty() -> null
        mediaUrl.startsWith("http") -> mediaUrl
        mediaUrl.startsWith("/") -> "$BASE_URL$mediaUrl"
        else -> "$BASE_URL/$mediaUrl"
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (!fullUrl.isNullOrEmpty()) {
            AsyncImage(
                model = fullUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (mediaType?.lowercase() == "video") {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Video",
                        tint = Color.White
                    )
                }
            }
        } else {
            Icon(
                imageVector = Icons.Default.ShoppingBag,
                contentDescription = "No image available",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}