package com.das3kn.iz.ui.presentation.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.das3kn.iz.R
import com.das3kn.iz.data.model.Post

@Composable
fun ListItem(
    post: Post,
    currentUserId: String,
    onLike: () -> Unit,
    modifier: Modifier = Modifier
) {
    val annotatedText = buildAnnotatedString {
        val username = if (post.username.isNotBlank()) post.username else "Kullanıcı"
        append(username)
        addStyle(SpanStyle(fontWeight = FontWeight.Bold), 0, username.length)
        append(" bir gönderi paylaştı.")
    }
    
    Column {
        Divider(
            color = Color.LightGray,
            thickness = 0.25.dp,
        )
        Column(
            modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
            ) {
                Image(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp)
                )
                Column(
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = annotatedText,
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.roboto_medium))
                        )
                    )
                    Text(
                        text = formatTimeAgo(post.createdAt),
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.roboto_medium))
                        ),
                        color = Color.LightGray
                    )
                }
            }

            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.roboto_medium))
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Media content
            if (post.mediaUrls.isNotEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.worker_image),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.FillWidth
                )
            }
        }

        ContentFunctions(
            onComment = { /*TODO*/ },
            onLike = { onLike() },
            onRepost = { /*TODO*/ },
            onMore = { /*TODO*/},
            isLiked = post.likes.contains(currentUserId),
            likeCount = post.likes.size
        )

        Divider(
            color = Color.LightGray,
            thickness = 0.25.dp
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
    // Preview için dummy post oluştur
    val dummyPost = com.das3kn.iz.data.model.Post(
        id = "1",
        userId = "user1",
        username = "TestUser",
        content = "Bu bir test gönderisidir.",
        createdAt = System.currentTimeMillis()
    )
    ListItem(
        post = dummyPost,
        currentUserId = "user1",
        onLike = { /* Preview */ }
    )
}