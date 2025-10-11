package com.das3kn.iz.ui.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.painterResource
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
    val friendshipState by viewModel.friendshipState.collectAsState()

    val isOtherUserProfile = userId != null
    val targetUserId = remember(userId, currentUserId) {
        userId ?: currentUserId
    }

    LaunchedEffect(targetUserId) {
        targetUserId?.let { id ->
            viewModel.loadUser(id)
            viewModel.loadUserPosts(id)
        }
    }

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

                        if (isOtherUserProfile) {
                            Spacer(modifier = Modifier.height(16.dp))
                            FriendshipAction(
                                state = friendshipState,
                                onAddFriend = {
                                    userState.user?.id?.let { viewModel.sendFriendRequest(it) }
                                },
                                onAcceptRequest = {
                                    userState.user?.id?.let { viewModel.acceptFriendRequest(it) }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                UserOptionsItem(
                                    text = "Arkadaşlar",
                                    icon = R.drawable.person_virtual_reality_svgrepo_com
                                ) {
                                    navController.navigate(MainNavTarget.PeopleScreen.route)
                                }
                                UserOptionsItem(
                                    text = "Gruplar",
                                    icon = R.drawable.group_svgrepo_com
                                ) {
                                    navController.navigate(MainNavTarget.GroupsScreen.route)
                                }
                                UserOptionsItem(
                                    text = "Kaydedilenler",
                                    icon = R.drawable.bookmark_svgrepo_com
                                ) {
                                    navController.navigate(MainNavTarget.SavedPostsScreen.route)
                                }
                                UserOptionsItem(
                                    text = "Medya",
                                    icon = R.drawable.media_library_svgrepo_com,
                                    onClick = null
                                )
                            }
                        }
                    }
                }
            }

            if (targetUserId != null || postsState.isLoading || postsState.error != null) {
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
                            onProfileClick = { userId ->
                                if (userId == currentUserId) {
                                    navController.navigate(MainNavTarget.ProfileScreen.route)
                                } else {
                                    navController.navigate("${MainNavTarget.ProfileScreen.route}/$userId")
                                }
                            },
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileCard(
    title: String,
    user: User?,
    isLoading: Boolean,
    error: String?
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            val username = user?.username?.takeIf { it.isNotBlank() }
            if (username != null) {
                Text(
                    text = "@$username",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            when {
                isLoading -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                }

                error != null -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                user != null && user.bio.isNotBlank() -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun UserOptionsItem(
    text: String,
    icon: Int,
    onClick: (() -> Unit)? = {}
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Column(
        modifier = clickableModifier
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = text,
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun FriendshipAction(
    state: FriendshipState,
    onAddFriend: () -> Unit,
    onAcceptRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            state.isFriend -> {
                Button(
                    onClick = {},
                    enabled = false
                ) {
                    Text(text = "Arkadaşsınız")
                }
            }

            state.hasIncomingRequest -> {
                Text(
                    text = "Bu kullanıcı sana arkadaşlık isteği gönderdi",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Button(onClick = onAcceptRequest) {
                    Text(text = "İsteği Kabul Et")
                }
            }

            state.isRequestPending -> {
                Text(
                    text = "Arkadaşlık isteğin gönderildi",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedButton(
                    onClick = {},
                    enabled = false
                ) {
                    Text(text = "İstek gönderildi")
                }
            }

            else -> {
                Button(onClick = onAddFriend) {
                    Text(text = "Arkadaş Ekle")
                }
            }
        }
    }
}