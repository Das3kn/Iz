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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.das3kn.iz.R

@Composable
fun ListItem(modifier: Modifier = Modifier) {
    val annotatedText = buildAnnotatedString {
        append("Buğra ")
        addStyle(SpanStyle(fontWeight = FontWeight.Bold), 0, 5)
        append("bir gönderi paylaştı.")
    }
    Column {
        Divider(
            color = Color.DarkGray,
            thickness = 0.5.dp,
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
                        text = annotatedText
                    )
                    Text(
                        text = "25 dakika önce",
                        color = Color.LightGray
                    )
                }
            }
            Image(
                painter = painterResource(id = R.drawable.worker_image),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.FillWidth
            )
        }
        Row(
            modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.empty_like_icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.comment_svgrepo_com),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.repost_svgrepo_com),
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )

            Image(
                imageVector = ImageVector.vectorResource(id = R.drawable.options_svgrepo_com),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
        Divider(
            color = Color.DarkGray,
            thickness = 0.5.dp
        )
    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ListItemPreview() {
    ListItem()
}