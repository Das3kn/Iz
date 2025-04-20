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

@Composable
fun ListItem(modifier: Modifier = Modifier) {
    val annotatedText = buildAnnotatedString {
        append("Buğra ")
        addStyle(SpanStyle(fontWeight = FontWeight.Bold), 0, 5)
        append("bir gönderi paylaştı.")
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
                        text = "25 dakika önce",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.roboto_medium))
                        ),
                        color = Color.LightGray
                    )
                }
            }

            Text(
                text = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s,",
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.roboto_medium))
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Image(
                painter = painterResource(id = R.drawable.worker_image),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.FillWidth
            )
        }

        ContentFunctions(
            onComment = { /*TODO*/ },
            onLike = { /*TODO*/ },
            onRepost = { /*TODO*/ },
            onMore = { /*TODO*/}
        )

        Divider(
            color = Color.LightGray,
            thickness = 0.25.dp
        )
    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun ListItemPreview() {
    ListItem()
}