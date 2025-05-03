package com.das3kn.iz.ui.presentation.people

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.das3kn.iz.ui.theme.components.textField.TextFieldWithEndIcon

@Composable
fun PeopleScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 12.dp)
    ) {
        TextFieldWithEndIcon(
            value = "",
            onValueChange = {},
            trailingIcon = Icons.Filled.Search
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // 2 item yan yana
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(6) { person ->
                PeopleItem(){
                    navController.navigate(MainNavTarget.ProfileScreen.route)
                }
            }
        }
    }

}

@Composable
fun PeopleItem(
    onClick: () -> Unit = {}
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
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color.White)
                        .size(54.dp)
                )
            }
            Text(
                text = "Ata Berk Aksoy",
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "10",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.roboto_medium)),
                            fontSize = 16.sp
                        ),
                        color = Color.Black,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Arkadaş",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.roboto_medium)),
                            fontSize = 10.sp
                        ),
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(start = 12.dp)
                ) {
                    Text(
                        text = "2",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.roboto_medium)),
                            fontSize = 16.sp
                        ),
                        color = Color.Black,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Grup",
                        style = TextStyle(
                            fontFamily = FontFamily(Font(R.font.roboto_medium)),
                            fontSize = 10.sp
                        ),
                        color = Color.Gray,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            


            SecondaryButton(
                onClick = { onClick.invoke() },
                text = "Profile Git",
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .fillMaxWidth(0.7f)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PeopleScreenPreview() {
}