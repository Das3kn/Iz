package com.das3kn.iz.ui.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.das3kn.iz.R
import com.das3kn.iz.data.model.Post
import com.das3kn.iz.ui.presentation.components.ImagePreviewDialog
import com.das3kn.iz.ui.presentation.components.PostMediaGallery
import com.das3kn.iz.ui.presentation.components.VideoPlayerDialog
import kotlin.math.max
import androidx.compose.ui.res.painterResource

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
    val userHandle = displayPost.username.takeIf { it.isNotBlank() }?.let { "@" + it }
    val userId = displayPost.userId
    val isRepost = post.repostOfPostId != null
    val repostDisplayName = post.repostedByDisplayName?.takeIf { it.isNotBlank() }
        ?: post.repostedByUsername?.takeIf { it.isNotBlank() }
        ?: post.username.takeIf { it.isNotBlank() }
        ?: displayName

    var selectedVideoUrl by remember { mutableStateOf<String?>(null) }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (isRepost) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.repost_svgrepo_com),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$repostDisplayName yeniden paylaştı",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            ProfileAvatar(
                imageUrl = displayPost.userProfileImage,
                initials = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                onClick = { if (userId.isNotBlank()) onProfileClick(userId) }
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.clickable { if (userId.isNotBlank()) onProfileClick(userId) }
                    )
                    userHandle?.let {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "·",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = formatTimeAgo(displayPost.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (displayPost.content.isNotBlank()) {
                    Text(
                        text = displayPost.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (displayPost.mediaUrls.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    PostMediaGallery(
                        mediaUrls = displayPost.mediaUrls,
                        mediaType = displayPost.mediaType,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                        onVideoClick = { selectedVideoUrl = it },
                        onImageClick = { selectedImageUrl = it }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                ContentFunctions(
                    onComment = { onComment(displayPost) },
                    onLike = { onLike(displayPost) },
                    onRepost = { onRepost(displayPost) },
                    onSave = { onSave(displayPost) },
                    isLiked = displayPost.likes.contains(currentUserId),
                    isSaved = displayPost.saves.contains(currentUserId),
                    likeCount = displayPost.likes.size,
                    commentCount = displayPost.commentCount,
                    repostCount = displayPost.shares,
                    saveCount = displayPost.saves.size,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Divider(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
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

@Composable
private fun ProfileAvatar(
    imageUrl: String,
    initials: String,
    onClick: () -> Unit
) {
    val outlineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .border(width = 1.dp, color = outlineColor, shape = CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )
        } else {
            Text(
                text = initials,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun formatTimeAgo(timestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    val diff = currentTime - timestamp
    val minute = 60_000
    val hour = 60 * minute
    val day = 24 * hour
    val month = 30 * day

    return when {
        diff < minute -> "Şimdi"
        diff < hour -> "${max(1, diff / minute)} dk"
        diff < day -> "${max(1, diff / hour)} sa"
        diff < month -> "${max(1, diff / day)} gün"
        else -> "${max(1, diff / month)} ay"
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
