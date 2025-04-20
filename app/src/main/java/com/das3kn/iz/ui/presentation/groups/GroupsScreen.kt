package com.das3kn.iz.ui.presentation.groups

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.das3kn.iz.R
import com.das3kn.iz.ui.presentation.navigation.MainNavTarget
import com.das3kn.iz.ui.theme.components.button.SecondaryButton

@Composable
fun GroupsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController

) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        items(6){
            Group(
                users = arrayListOf("Kullanıcı","Kullanıcı","Kullanıcı","Kullanıcı","Kullanıcı"),
                onClick = {
                    navController.navigate(MainNavTarget.GroupContentScreen.route)
                }
            )
        }
    }
}

@Composable
fun Group(
    users: ArrayList<String>,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .shadow(4.dp, shape = RoundedCornerShape(12.dp))
            .fillMaxWidth(0.5f)
            .padding(3.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp)) // Shadow'u kesmeden içeriyi kliple
                .background(Color.White) // Arka plan belirlemek gölgeyi daha net gösterir
                .padding(8.dp)
        ) {
            Box {
                Image(
                    painter = painterResource(id = R.drawable.worker_image),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .height(64.dp)
                        .fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 32.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .size(54.dp)
                    )
                }
            }

            Text(
                text = "Group Name",
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.roboto_medium))
                )
            )
            Text(
                text = "2 saat önce aktifti",
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.roboto_medium)),
                    fontSize = 10.sp
                ),
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row {
                users.forEach {
                    Image(
                        imageVector = Icons.Filled.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(top = 8.dp, start = 2.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .size(24.dp)
                    )
                }
            }
            Text(
                text = "Public Group / 5 Members",
                style = TextStyle(
                    fontFamily = FontFamily(Font(R.font.roboto_medium)),
                    fontSize = 10.sp
                ),
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )

            SecondaryButton(
                onClick = { onClick.invoke() },
                text = "View Group",
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .fillMaxWidth(0.7f)
            )
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GroupsScreenPreview() {
    //GroupsScreen(navController = navController)
}