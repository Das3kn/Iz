package com.das3kn.iz.ui.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.das3kn.iz.R
import com.das3kn.iz.data.model.User

@Composable
fun GroupsContentScreen(modifier: Modifier = Modifier, navController: NavHostController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ProfileCard(
            title = "Grup Adı"
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            UserOptionsItem(text = "Arkadaşlar", icon = R.drawable.person_virtual_reality_svgrepo_com)
            UserOptionsItem(text = "Gruplar", icon = R.drawable.group_svgrepo_com)
            UserOptionsItem(text = "Forumlar", icon = R.drawable.comment_forum_svgrepo_com)
            UserOptionsItem(text = "Medya", icon = R.drawable.media_library_svgrepo_com)
        }
    }
}

@Composable
fun UserOptionsItem(
    text: String,
    icon: Int,
    onClick: () -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Image(
            imageVector = ImageVector.vectorResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            style = TextStyle(
                fontFamily = FontFamily(Font(R.font.roboto_medium)),
                color = Color(0xFF444444),
                fontSize = 10.sp
            ),

        )
    }
}

@Composable
fun ProfileCard(
    modifier: Modifier = Modifier, 
    title: String,
    user: User? = null,
    isLoading: Boolean = false,
    error: String? = null
) {
    Box(
        contentAlignment = Alignment.BottomCenter
    ) {
        Image(
            painter = painterResource(id = R.drawable.worker_image),
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentScale = ContentScale.FillWidth
        )

        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .padding(bottom = 16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(top = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                    contentColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 40.dp, bottom = 16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else if (error != null) {
                        Text(
                            text = "Hata: $error",
                            color = Color.Red
                        )
                    } else {
                        Text(
                            text = user?.displayName ?: title,
                            style = TextStyle(
                                fontFamily = FontFamily(Font(R.font.roboto_medium)),
                                color = Color.Black
                            )
                        )
                        
                        if (user?.username?.isNotEmpty() == true) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "@${user.username}",
                                style = TextStyle(
                                    fontFamily = FontFamily(Font(R.font.roboto_medium)),
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            )
                        }
                    }
                }
            }

            if (user != null && !isLoading && error == null) {
                // Kullanıcı profil resmi
                Box(
                    modifier = Modifier
                        .offset(y = (-27).dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.displayName.firstOrNull()?.uppercase() ?: "?",
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Image(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .offset(y = (-27).dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .size(100.dp)
                )
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GroupsContentScreenPreview() {
}