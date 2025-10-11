package com.das3kn.iz.ui.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Message
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.das3kn.iz.data.model.User
import com.das3kn.iz.ui.presentation.home.components.ListItem
import com.das3kn.iz.ui.presentation.navigation.MainNavTarget
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

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
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = userState.user?.displayName ?: "Profil",
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${postsState.posts.size} paylaşım",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
        val backgroundColor = Color(0xFFF3F4F6)
        val user = userState.user

        when {
            userState.isLoading && user == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            userState.error != null && user == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userState.error ?: "Profil yüklenemedi",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(backgroundColor)
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    user?.let { loadedUser ->
                        item {
                            ProfileHeaderSection(
                                user = loadedUser,
                                postCount = postsState.posts.size,
                                isOwnProfile = loadedUser.id == currentUserId,
                                friendshipState = friendshipState,
                                onAddFriend = { viewModel.sendFriendRequest(loadedUser.id) },
                                onAcceptRequest = { viewModel.acceptFriendRequest(loadedUser.id) }
                            )
                        }
                    }

                    item {
                        PostsHeader()
                    }

                    when {
                        postsState.isLoading -> {
                            item {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                        }

                        postsState.error != null -> {
                            item {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text(
                                        text = "Hata: ${postsState.error}",
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }

                        postsState.posts.isEmpty() -> {
                            item {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    shape = MaterialTheme.shapes.medium
                                ) {
                                    Text(
                                        text = "Henüz paylaşım yok",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 32.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        else -> {
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
    }
}

@Composable
private fun ProfileHeaderSection(
    user: User,
    postCount: Int,
    isOwnProfile: Boolean,
    friendshipState: FriendshipState,
    onAddFriend: () -> Unit,
    onAcceptRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF8B5CF6),
                                Color(0xFF22D3EE)
                            )
                        )
                    )
            ) {
                ProfileAvatar(
                    user = user,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp)
                        .offset(y = 60.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 72.dp, bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = user.displayName.ifBlank { "İsimsiz Kullanıcı" },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (user.username.isNotBlank()) {
                            Text(
                                text = "@${user.username}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    if (isOwnProfile) {
                        OutlinedButton(
                            onClick = {},
                            shape = CircleShape
                        ) {
                            Text(text = "Profili Düzenle")
                        }
                    } else {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            MessageButton()
                            FollowActionButton(
                                state = friendshipState,
                                onAddFriend = onAddFriend,
                                onAcceptRequest = onAcceptRequest
                            )
                        }
                    }
                }

                if (user.bio.isNotBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = user.bio,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (!isOwnProfile && friendshipState.hasIncomingRequest) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Bu kullanıcı sana arkadaşlık isteği gönderdi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    ProfileStatisticItem(label = "Paylaşım", value = postCount)
                    ProfileStatisticItem(label = "Takip", value = user.following.size)
                    ProfileStatisticItem(label = "Takipçi", value = user.followers.size)
                }
            }
        }
    }
}

@Composable
private fun MessageButton(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.size(44.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        IconButton(onClick = { /* Mesajlaşma gelecekte eklenecek */ }) {
            Icon(
                imageVector = Icons.Outlined.Message,
                contentDescription = "Mesaj Gönder"
            )
        }
    }
}

@Composable
private fun FollowActionButton(
    state: FriendshipState,
    onAddFriend: () -> Unit,
    onAcceptRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        state.hasIncomingRequest -> {
            Button(
                onClick = onAcceptRequest,
                shape = CircleShape,
                modifier = modifier
            ) {
                Text(text = "İsteği Kabul Et")
            }
        }

        state.isFriend -> {
            Button(
                onClick = {},
                enabled = false,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = modifier
            ) {
                Text(text = "Takip Ediliyor")
            }
        }

        state.isRequestPending -> {
            Button(
                onClick = {},
                enabled = false,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = modifier
            ) {
                Text(text = "İstek Gönderildi")
            }
        }

        else -> {
            Button(
                onClick = onAddFriend,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = modifier
            ) {
                Icon(
                    imageVector = Icons.Outlined.PersonAdd,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Takip Et")
            }
        }
    }
}

@Composable
private fun ProfileAvatar(user: User, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .size(120.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(4.dp, Color.White),
        tonalElevation = 4.dp
    ) {
        if (user.profileImageUrl.isNotBlank()) {
            AsyncImage(
                model = user.profileImageUrl,
                contentDescription = user.displayName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ProfileStatisticItem(label: String, value: Int) {
    Column {
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PostsHeader() {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Text(
            text = "Paylaşımlar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            textAlign = TextAlign.Center
        )
    }
}
