package com.das3kn.iz.ui.presentation.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.das3kn.iz.R

@Composable
fun ContentFunctions(
    onLike: () -> Unit,
    onComment: () -> Unit,
    onRepost: () -> Unit,
    onMore: () -> Unit,
    isLiked: Boolean = false,
    likeCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Like button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onLike() }
        ) {
            Image(
                imageVector = ImageVector.vectorResource(
                    id = if (isLiked) R.drawable.like_icon else R.drawable.empty_like_icon
                ),
                contentDescription = if (isLiked) "Beğeniyi kaldır" else "Beğen",
                modifier = Modifier.size(24.dp)
            )
            if (likeCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = likeCount.toString(),
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Comment button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onComment() }
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.comment_svgrepo_com),
                contentDescription = "Yorum yap",
                modifier = Modifier.size(24.dp)
            )
        }

        // Repost button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onRepost() }
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.repost_svgrepo_com),
                contentDescription = "Yeniden paylaş",
                modifier = Modifier.size(24.dp)
            )
        }

        // More options button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onMore() }
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.options_svgrepo_com),
                contentDescription = "Daha fazla seçenek",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}