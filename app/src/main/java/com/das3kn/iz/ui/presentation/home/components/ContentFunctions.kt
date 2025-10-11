package com.das3kn.iz.ui.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.das3kn.iz.R

@Composable
fun ContentFunctions(
    onLike: () -> Unit,
    onComment: () -> Unit,
    onRepost: () -> Unit,
    onSave: () -> Unit,
    isLiked: Boolean = false,
    isSaved: Boolean = false,
    likeCount: Int = 0,
    commentCount: Int = 0,
    repostCount: Int = 0,
    saveCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val defaultColor = MaterialTheme.colorScheme.onSurfaceVariant
    val likeColor = Color(0xFFE0245E)
    val repostColor = Color(0xFF16A34A)
    val saveColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 360.dp)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        PostActionButton(
            onClick = onComment,
            icon = painterResource(id = R.drawable.comment_svgrepo_com),
            contentDescription = "Yorum yap",
            count = commentCount,
            tint = defaultColor,
        )

        PostActionButton(
            onClick = onRepost,
            icon = painterResource(id = R.drawable.repost_svgrepo_com),
            contentDescription = "Yeniden paylaş",
            count = repostCount,
            tint = repostColor,
            inactiveTint = defaultColor,
            isActive = false
        )

        PostActionButton(
            onClick = onLike,
            icon = painterResource(
                id = if (isLiked) R.drawable.like_icon else R.drawable.empty_like_icon
            ),
            contentDescription = if (isLiked) "Beğeniyi kaldır" else "Beğen",
            count = likeCount,
            tint = likeColor,
            inactiveTint = defaultColor,
            isActive = isLiked
        )

        PostActionButton(
            onClick = onSave,
            icon = painterResource(
                id = if (isSaved) R.drawable.bookmark_filled else R.drawable.bookmark_outline
            ),
            contentDescription = if (isSaved) "Kaydı kaldır" else "Kaydet",
            count = saveCount,
            tint = saveColor,
            inactiveTint = defaultColor,
            isActive = isSaved
        )
    }
}

@Composable
private fun PostActionButton(
    onClick: () -> Unit,
    icon: Painter,
    contentDescription: String?,
    count: Int,
    tint: Color,
    inactiveTint: Color = tint,
    isActive: Boolean = false
) {
    val displayColor = if (isActive) tint else inactiveTint

    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = contentDescription,
            tint = displayColor,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.bodySmall,
            color = displayColor,
            modifier = Modifier.padding(start = 6.dp)
        )
    }
}