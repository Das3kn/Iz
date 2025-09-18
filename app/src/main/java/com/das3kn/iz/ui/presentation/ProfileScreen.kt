package com.das3kn.iz.ui.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.das3kn.iz.R
import com.das3kn.iz.ui.presentation.navigation.MainNavTarget

@Composable
fun ProfileScreen(modifier: Modifier = Modifier, navController: NavHostController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProfileCard(title = "Buğra Karagözoğlu")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                UserOptionsItem(text = "Arkadaşlar", icon = R.drawable.person_virtual_reality_svgrepo_com){
                    navController.navigate(MainNavTarget.PeopleScreen.route)
                }
                UserOptionsItem(text = "Gruplar", icon = R.drawable.group_svgrepo_com){
                    navController.navigate(MainNavTarget.GroupsScreen.route)
                }
                UserOptionsItem(text = "Kaydedilenler", icon = R.drawable.bookmark_svgrepo_com){
                    navController.navigate(MainNavTarget.SavedPostsScreen.route)
                }
                UserOptionsItem(text = "Medya", icon = R.drawable.media_library_svgrepo_com)
            }
        }
    }
}