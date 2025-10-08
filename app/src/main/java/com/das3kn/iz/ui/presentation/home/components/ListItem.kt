package com.das3kn.iz.ui.presentation.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.das3kn.iz.R
import com.das3kn.iz.data.model.Post
import com.das3kn.iz.ui.presentation.components.ImagePreviewDialog
import com.das3kn.iz.ui.presentation.components.PostMediaGallery
import com.das3kn.iz.ui.presentation.components.VideoPlayerDialog

@Composable
fun ListItem(
    post: Post,
    currentUserId: String,
    onLike: (Post) -> Unit,
    onComment: (Post) -> Unit = {},
    onSave: (Post) -> Unit = {},
    onRepost: (Post) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val displayPost = post.originalPost ?: post
    val username = if (displayPost.username.isNotBlank()) displayPost.username else "Kullanıcı"
    val isRepost = post.repostOfPostId != null
    val repostDisplayName = post.repostedByDisplayName?.takeIf { it.isNotBlank() }
        ?: post.repostedByUsername?.takeIf { it.isNotBlank() }
        ?: post.username.takeIf { it.isNotBlank() }
        ?: username

    var selectedVideoUrl by remember { mutableStateOf<String?>(null) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (isRepost) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(id = R.drawable.repost_svgrepo_com),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$repostDisplayName yeniden paylaştı",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 2.dp
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                CircleShape
                            )
                            .padding(2.dp)
                            .background(
                                MaterialTheme.colorScheme.surface,
                                CircleShape
                            )
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = username,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formatTimeAgo(displayPost.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (displayPost.content.isNotBlank()) {
                    Text(
                        text = displayPost.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                if (displayPost.mediaUrls.isNotEmpty()) {
                    PostMediaGallery(
                        mediaUrls = displayPost.mediaUrls,
                        mediaType = displayPost.mediaType,
                        onVideoClick = { selectedVideoUrl = it },
                        onImageClick = { selectedImageUrl = it }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                ContentFunctions(
                    onComment = { onComment(displayPost) },
                    onLike = { onLike(displayPost) },
                    onRepost = { onRepost(displayPost) },
                    onSave = { onSave(displayPost) },
                    onMore = { /*TODO*/ },
                    isLiked = displayPost.likes.contains(currentUserId),
                    isSaved = displayPost.saves.contains(currentUserId),
                    likeCount = displayPost.likes.size,
                    commentCount = displayPost.commentCount,
                    saveCount = displayPost.saves.size
                )
            }
        }
    }

    selectedVideoUrl?.let { url ->
        VideoPlayerDialog(
            videoUrl = url,
            onDismiss = { selectedVideoUrl = null }
        )
    }

    selectedImageUrl?.let { url ->
        ImagePreviewDialog(
            imageUrl = url,
            onDismiss = { selectedImageUrl = null }
        )
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    val diff = currentTime - timestamp

    return when {
        diff < 60000 -> "Az önce"
        diff < 3600000 -> "${diff / 60000} dakika önce"
        diff < 86400000 -> "${diff / 3600000} saat önce"
        diff < 2592000000 -> "${diff / 86400000} gün önce"
        else -> "${diff / 2592000000} ay önce"
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ListItemPreview() {
    val dummyPost = Post(
        id = "1",
        userId = "user1",
        username = "TestUser",
        content = "Bu bir test gönderisidir.",
        createdAt = System.currentTimeMillis()
    )
    ListItem(
        post = dummyPost,
        currentUserId = "user1",
        onLike = {}
    )
}
