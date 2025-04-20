package com.das3kn.iz.ui.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.das3kn.iz.R

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
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
                UserOptionsItem(text = "Arkadaşlar", icon = R.drawable.person_virtual_reality_svgrepo_com)
                UserOptionsItem(text = "Gruplar", icon = R.drawable.group_svgrepo_com)
                UserOptionsItem(text = "Forumlar", icon = R.drawable.comment_forum_svgrepo_com)
                UserOptionsItem(text = "Medya", icon = R.drawable.media_library_svgrepo_com)
            }
        }
    }
}