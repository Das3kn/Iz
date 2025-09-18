package com.das3kn.iz.ui.presentation.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.das3kn.iz.R

@Composable
fun ContentFunctions(
    onLike: () -> Unit,
    onComment: () -> Unit,
    onRepost: () -> Unit,
    onSave: () -> Unit,
    onMore: () -> Unit,
    isLiked: Boolean = false,
    isSaved: Boolean = false,
    likeCount: Int = 0,
    commentCount: Int = 0,
    saveCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Like button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onLike() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .background(
                    if (isLiked) 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else 
                        Color.Transparent,
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Image(
                imageVector = ImageVector.vectorResource(
                    id = if (isLiked) R.drawable.like_icon else R.drawable.empty_like_icon
                ),
                contentDescription = if (isLiked) "Beğeniyi kaldır" else "Beğen",
                modifier = Modifier.size(20.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                    if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            if (likeCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = likeCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Comment button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onComment() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .background(
                    Color.Transparent,
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.comment_svgrepo_com),
                contentDescription = "Yorum yap",
                modifier = Modifier.size(20.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
            )
            if (commentCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = commentCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Repost button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onRepost() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .background(
                    Color.Transparent,
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.repost_svgrepo_com),
                contentDescription = "Yeniden paylaş",
                modifier = Modifier.size(20.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        // Save button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onSave() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .background(
                    if (isSaved) 
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else 
                        Color.Transparent,
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Image(
                imageVector = ImageVector.vectorResource(
                    id = if (isSaved) R.drawable.bookmark_filled else R.drawable.bookmark_outline
                ),
                contentDescription = if (isSaved) "Kaydı kaldır" else "Kaydet",
                modifier = Modifier.size(20.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(
                    if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            if (saveCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = saveCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // More options button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onMore() }
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .background(
                    Color.Transparent,
                    RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.options_svgrepo_com),
                contentDescription = "Daha fazla seçenek",
                modifier = Modifier.size(20.dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }
    }
}