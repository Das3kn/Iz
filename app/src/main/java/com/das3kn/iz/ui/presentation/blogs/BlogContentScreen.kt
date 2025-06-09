package com.das3kn.iz.ui.presentation.blogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.das3kn.iz.R

@Composable
fun BlogContentScreen(modifier: Modifier = Modifier, navController: NavHostController) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        BlogDetail(
            title = "Using Social Network Properly For Businesses",
            writer = "Das3kn",
            date = "24.01.2020",
            category = "Marketing",
            file = "Technology"
        )

        Image(
            painter = painterResource(id = R.drawable.worker_image),
            contentDescription = null,
            modifier = Modifier
                .padding(vertical = 22.dp)
                .clip(RoundedCornerShape(12.dp))
                .fillMaxWidth()
            ,
            contentScale = ContentScale.FillWidth
        )

        Text(
            text = "Benefits of social media for brand building",
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.roboto_medium)),
                fontSize = 32.sp
            ),
            color = Color.DarkGray,
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )

        Text(
            text = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.",
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.roboto_medium)),
                fontSize = 18.sp,
            ),
            color = Color.DarkGray,
            modifier = Modifier
                .padding(start = 4.dp)
        )

    }
}

@Composable
fun BlogDetail(
    title: String,
    writer: String,
    date: String,
    category: String,
    file: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = "By: ${writer}",
        style = TextStyle(
            fontFamily = FontFamily(Font(R.font.roboto_medium)),
            fontSize = 14.sp
        ),
        textAlign = TextAlign.Center,
        color = Color.LightGray,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp)
    )

    Text(
        text = title,
        style = TextStyle(
            fontFamily = FontFamily(Font(R.font.roboto_medium)),
            fontSize = 32.sp
        ),
        color = Color.DarkGray,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .padding(start = 14.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        BlogDetailItem(
            icon = Icons.Default.DateRange,
            title = date
        )

        BlogDetailItem(
            icon = Icons.Default.DateRange,
            title = file,
            onClick = {}
        )

        BlogDetailItem(
            icon = Icons.Default.DateRange,
            title = category,
            onClick = {}
        )
    }
}

@Composable
fun BlogDetailItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit = {},
    enabled: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(enabled = enabled, onClick = onClick)
    ) {
        Image(
            imageVector = icon,
            contentDescription = null,
            colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.LightGray)
        )

        Text(
            text = title,
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.roboto_medium)),
                fontSize = 14.sp
            ),
            color = Color.LightGray,
            modifier = Modifier
                .padding(start = 4.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BlogContentPreview() {
    //BlogContentScreen(navController = navController)
}