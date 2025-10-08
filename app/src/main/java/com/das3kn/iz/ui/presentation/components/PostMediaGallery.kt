package com.das3kn.iz.ui.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.das3kn.iz.R
import com.das3kn.iz.data.model.MediaType

@Composable
fun PostMediaGallery(
    mediaUrls: List<String>,
    mediaType: MediaType,
    modifier: Modifier = Modifier,
    onVideoClick: (String) -> Unit = {}
) {
    if (mediaUrls.isEmpty()) return

    when (mediaType) {
        MediaType.IMAGE -> renderImages(mediaUrls, modifier)
        MediaType.VIDEO -> renderVideos(mediaUrls, modifier, onVideoClick)
        MediaType.MIXED -> renderMixed(mediaUrls, modifier, onVideoClick)
        else -> Unit
    }
}

@Composable
private fun renderImages(
    mediaUrls: List<String>,
    modifier: Modifier
) {
    if (mediaUrls.size == 1) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            AsyncImage(
                model = mediaUrls.first(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
    } else {
        LazyRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mediaUrls) { imageUrl ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun renderVideos(
    mediaUrls: List<String>,
    modifier: Modifier,
    onVideoClick: (String) -> Unit
) {
    if (mediaUrls.size == 1) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            VideoThumbnail(
                videoUrl = mediaUrls.first(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                onClick = onVideoClick
            )
        }
    } else {
        LazyRow(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mediaUrls) { videoUrl ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    VideoThumbnail(
                        videoUrl = videoUrl,
                        modifier = Modifier.size(120.dp),
                        onClick = onVideoClick
                    )
                }
            }
        }
    }
}

@Composable
private fun renderMixed(
    mediaUrls: List<String>,
    modifier: Modifier,
    onVideoClick: (String) -> Unit
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(mediaUrls) { mediaUrl ->
            val isVideo = isVideoUrl(mediaUrl)
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                if (isVideo) {
                    VideoThumbnail(
                        videoUrl = mediaUrl,
                        modifier = Modifier.size(120.dp),
                        onClick = onVideoClick
                    )
                } else {
                    AsyncImage(
                        model = mediaUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoThumbnail(
    videoUrl: String,
    modifier: Modifier = Modifier,
    onClick: (String) -> Unit
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable { onClick(videoUrl) },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = videoUrl,
            contentDescription = "Video",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.video)
        )
        Icon(
            imageVector = Icons.Default.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = Color.White
        )
    }
}

private fun isVideoUrl(url: String): Boolean {
    val lower = url.lowercase()
    val videoExtensions = listOf(".mp4", ".m4v", ".mov", ".avi", ".mkv", ".webm")
    return videoExtensions.any { lower.contains(it) }
}
