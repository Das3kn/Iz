package com.das3kn.iz.ui.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.das3kn.iz.R
import com.das3kn.iz.data.model.Post
import com.das3kn.iz.data.model.User
import com.das3kn.iz.ui.presentation.home.components.ListItem
import com.das3kn.iz.ui.presentation.navigation.MainNavTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier, 
    navController: NavHostController,
    userId: String? = null,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val userState by viewModel.userState.collectAsState()
    val postsState by viewModel.postsState.collectAsState()
    val currentUserId by viewModel.currentUserId.collectAsState()

    LaunchedEffect(userId) {
        if (userId != null) {
            viewModel.loadUser(userId)
            viewModel.loadUserPosts(userId)
        }
    }

    val isOtherUserProfile = userId != null

    Scaffold(
        topBar = {
            if (isOtherUserProfile) {
                TopAppBar(
                    title = {
                        Text(
                            text = userState.user?.displayName ?: "Profil",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Kullanıcı bilgileri
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ProfileCard(
                            title = userState.user?.displayName ?: "Buğra Karagözoğlu",
                            user = userState.user,
                            isLoading = userState.isLoading,
                            error = userState.error
                        )
                        
                        if (!isOtherUserProfile) {
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
            }

            // Kullanıcının postları (sadece başka kullanıcının profili için)
            if (isOtherUserProfile) {
                item {
                    Text(
                        text = "Paylaşımlar",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                if (postsState.isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (postsState.error != null) {
                    item {
                        Text(
                            text = "Hata: ${postsState.error}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else if (postsState.posts.isEmpty()) {
                    item {
                        Text(
                            text = "Henüz paylaşım yok",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                } else {
                    items(postsState.posts) { post ->
                        ListItem(
                            post = post,
                            currentUserId = currentUserId ?: "",
                            onLike = { /* Like işlemi */ },
                            onComment = { /* Comment işlemi */ },
                            onSave = { /* Save işlemi */ },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}