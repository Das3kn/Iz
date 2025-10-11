package com.das3kn.iz.ui.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.das3kn.iz.R

@Composable
fun ContentFunctions(
    onLike: () -> Unit,
    onComment: () -> Unit,
    onRepost: () -> Unit,
    onSave: () -> Unit,
    isLiked: Boolean = false,
    isReposted: Boolean = false,
    isSaved: Boolean = false,
    likeCount: Int = 0,
    commentCount: Int = 0,
    repostCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val baseColor = MaterialTheme.colorScheme.onSurfaceVariant
    val commentColor = baseColor
    val repostColor = if (isReposted) MaterialTheme.colorScheme.primary else baseColor
    val likeColor = if (isLiked) MaterialTheme.colorScheme.secondary else baseColor
    val shareColor = if (isSaved) MaterialTheme.colorScheme.primary else baseColor

    Row(
        modifier = modifier
            .fillMaxWidth()
            .widthIn(max = 360.dp)
            .padding(top = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        InteractionButton(
            icon = ImageVector.vectorResource(id = R.drawable.comment_svgrepo_com),
            contentDescription = "Yorum yap",
            text = if (commentCount > 0) commentCount.toString() else null,
            onClick = onComment,
            color = commentColor
        )

        InteractionButton(
            icon = ImageVector.vectorResource(id = R.drawable.repost_svgrepo_com),
            contentDescription = "Yeniden paylaş",
            text = if (repostCount > 0) repostCount.toString() else null,
            onClick = onRepost,
            color = repostColor
        )

        InteractionButton(
            icon = ImageVector.vectorResource(
                id = if (isLiked) R.drawable.like_icon else R.drawable.empty_like_icon
            ),
            contentDescription = if (isLiked) "Beğeniyi kaldır" else "Beğen",
            text = if (likeCount > 0) likeCount.toString() else null,
            onClick = onLike,
            color = likeColor
        )

        ShareButton(
            onClick = onSave,
            color = shareColor
        )
    }
}

@Composable
private fun InteractionButton(
    icon: ImageVector,
    contentDescription: String,
    text: String?,
    onClick: () -> Unit,
    color: Color
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(20.dp),
            tint = color
        )

        text?.let {
            Spacer(modifier = Modifier.size(6.dp))
            Text(
                text = it,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
private fun ShareButton(
    onClick: () -> Unit,
    color: Color
) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Share,
            contentDescription = "Paylaş",
            modifier = Modifier.size(20.dp),
            tint = color
        )
    }
}
