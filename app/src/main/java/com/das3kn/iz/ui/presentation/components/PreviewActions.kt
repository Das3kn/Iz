package com.das3kn.iz.ui.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.das3kn.iz.R

@Composable
fun PreviewActionsRow(
    modifier: Modifier = Modifier,
    isLiked: Boolean,
    likeCount: Int,
    commentCount: Int,
    repostCount: Int,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onRepost: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PreviewAction(
            icon = if (isLiked) R.drawable.like_icon else R.drawable.empty_like_icon,
            contentDescription = if (isLiked) "Beğeniyi kaldır" else "Beğen",
            value = likeCount,
            isHighlighted = isLiked,
            onClick = onLike
        )

        PreviewAction(
            icon = R.drawable.comment_svgrepo_com,
            contentDescription = "Yorumlar",
            value = commentCount,
            onClick = onComment
        )

        PreviewAction(
            icon = R.drawable.repost_svgrepo_com,
            contentDescription = "Yeniden paylaş",
            value = repostCount,
            onClick = onRepost
        )
    }
}

@Composable
private fun PreviewAction(
    icon: Int,
    contentDescription: String,
    value: Int,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = icon),
            contentDescription = contentDescription,
            tint = if (isHighlighted) Color.White else Color.White.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value.toString(),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
