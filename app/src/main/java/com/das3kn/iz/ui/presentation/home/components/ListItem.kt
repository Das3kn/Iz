package com.das3kn.iz.ui.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.das3kn.iz.R
import com.das3kn.iz.data.model.Post
import com.das3kn.iz.ui.presentation.components.ImagePreviewDialog
import com.das3kn.iz.ui.presentation.components.PostMediaGallery
import com.das3kn.iz.ui.presentation.components.VideoPlayerDialog
import kotlin.math.max

@Composable
fun ListItem(
    post: Post,
    currentUserId: String,
    onLike: (Post) -> Unit,
    onComment: (Post) -> Unit = {},
    onSave: (Post) -> Unit = {},
    onRepost: (Post) -> Unit = {},
    onProfileClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val displayPost = post.originalPost ?: post
    val displayName = displayPost.username.ifBlank { "Kullanıcı" }
    val userId = displayPost.userId
    val isRepost = post.repostOfPostId != null
    val repostDisplayName = post.repostedByDisplayName?.takeIf { it.isNotBlank() }
        ?: post.repostedByUsername?.takeIf { it.isNotBlank() }
        ?: post.username.takeIf { it.isNotBlank() }
        ?: displayName
    val handle = displayPost.username
        .trim()
        .replace("\\s+".toRegex(), "")
        .lowercase()
        .takeIf { it.isNotBlank() }
        ?.let { "@$it" }
    val hasReposted =
        post.repostedByUserId == currentUserId || displayPost.repostedByUserId == currentUserId

    var selectedVideoUrl by remember { mutableStateOf<String?>(null) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {

        // Repost şeridi
        if (isRepost) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.repost_svgrepo_com),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$repostDisplayName yeniden paylaştı",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // İçerik alanı
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = if (isRepost) 12.dp else 8.dp, bottom = 16.dp)
        ) {

            // ÜST SATIR: Avatar + (Ad/Handle/Zaman + Metin)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable(enabled = userId.isNotBlank()) { onProfileClick(userId) },
                    contentAlignment = Alignment.Center
                ) {
                    if (displayPost.userProfileImage.isNotBlank()) {
                        AsyncImage(
                            model = displayPost.userProfileImage,
                            contentDescription = displayName,
                            modifier = Modifier.matchParentSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Sağ taraf (başlık satırı + içerik metni)
                Column(modifier = Modifier.weight(1f)) {

                    // (Ad, handle) solda; zaman bilgisi sağda
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = displayName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.clickable(enabled = userId.isNotBlank()) { onProfileClick(userId) }
                            )
                            handle?.let {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = it,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Zaman bilgisi – tek satır, sarmasız
                        Text(
                            text = formatTimeAgo(displayPost.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    // İçerik metni
                    if (displayPost.content.isNotBlank()) {
                        Text(
                            text = displayPost.content,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            // --- DİKKAT --- //
            // Medya ve aksiyonlar artık Row’un DIŞINDA (tam genişlikte)
            if (displayPost.mediaUrls.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                PostMediaGallery(
                    mediaUrls = displayPost.mediaUrls,
                    mediaType = displayPost.mediaType,
                    modifier = Modifier
                        .fillMaxWidth(),
                    onVideoClick = { selectedVideoUrl = it },
                    onImageClick = { selectedImageUrl = it }
                )
            }

            ContentFunctions(
                onComment = { onComment(displayPost) },
                onLike = { onLike(displayPost) },
                onRepost = { onRepost(displayPost) },
                onSave = { onSave(displayPost) },
                isLiked = displayPost.likes.contains(currentUserId),
                isReposted = hasReposted,
                isSaved = displayPost.saves.contains(currentUserId),
                likeCount = displayPost.likes.size,
                commentCount = displayPost.commentCount,
                repostCount = displayPost.shares,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    }

    // Dialoglar
    selectedVideoUrl?.let { url ->
        VideoPlayerDialog(videoUrl = url, onDismiss = { selectedVideoUrl = null })
    }
    selectedImageUrl?.let { url ->
        ImagePreviewDialog(imageUrl = url, onDismiss = { selectedImageUrl = null })
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    val diff = max(0L, currentTime - timestamp)
    val minuteMillis = 60_000L
    val hourMillis = 3_600_000L
    val dayMillis = 86_400_000L
    return when {
        diff < hourMillis -> "${(diff / minuteMillis).coerceAtLeast(1)} dk"
        diff < dayMillis -> "${(diff / hourMillis).coerceAtLeast(1)} sa"
        else -> "${(diff / dayMillis).coerceAtLeast(1)} gün"
    }
}

@Preview(showBackground = true)
@Composable
private fun ListItemPreview() {
    val dummyPost = Post(
        id = "1",
        userId = "user1",
        username = "Test Kullanıcı",
        content = "Bu, Compose ile oluşturulmuş örnek bir gönderidir.",
        createdAt = System.currentTimeMillis() - 90 * 60 * 1000,
        likes = listOf("user2", "user3"),
        commentCount = 12,
        shares = 4,
        mediaUrls = listOf() // örnek
    )
    ListItem(
        post = dummyPost,
        currentUserId = "user1",
        onLike = {},
        onComment = {},
        onSave = {},
        onRepost = {}
    )
}

