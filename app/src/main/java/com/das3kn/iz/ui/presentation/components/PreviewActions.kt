package com.das3kn.iz.ui.presentation.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlin.math.max
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
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.85f))
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 16.dp),
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
    modifier: Modifier = Modifier,
    icon: Int,
    contentDescription: String,
    value: Int,
    isHighlighted: Boolean = false,
    onClick: () -> Unit
) {
    val displayValue = max(value, 0)
    Row(
        modifier = modifier
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            imageVector = ImageVector.vectorResource(id = icon),
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
            colorFilter = ColorFilter.tint(
                if (isHighlighted) MaterialTheme.colorScheme.primary else Color.White
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = displayValue.toString(),
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Medium,
            modifier = Modifier.alpha(if (isHighlighted) 1f else 0.9f)
        )
    }
}
