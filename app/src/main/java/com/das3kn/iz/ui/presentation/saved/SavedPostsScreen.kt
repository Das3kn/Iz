package com.das3kn.iz.ui.presentation.saved

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.das3kn.iz.data.model.Post
import com.das3kn.iz.ui.presentation.home.components.ListItem
import com.das3kn.iz.ui.presentation.auth.AuthViewModel
import com.das3kn.iz.ui.presentation.navigation.MainNavTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPostsScreen(
    navController: NavHostController,
    viewModel: SavedPostsViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { userId ->
            viewModel.setCurrentUserId(userId)
            viewModel.loadSavedPosts()
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kaydedilenler",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Geri"
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(modifier = Modifier.padding(paddingValues)) {
                when {
                    uiState.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    uiState.error != null -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Hata: ${uiState.error}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                androidx.compose.material3.Button(
                                    onClick = { viewModel.loadSavedPosts() }
                                ) {
                                    Text("Tekrar Dene")
                                }
                            }
                        }
                    }
                    uiState.savedPosts.isEmpty() -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Favorite,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Henüz kaydedilen gönderi yok",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Beğendiğiniz gönderileri kaydetmek için kaydet ikonuna tıklayın",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                    else -> {
                        LazyColumn {
                            items(uiState.savedPosts) { post ->
                                SavedPostItem(
                                    post = post,
                                    currentUserId = currentUser?.uid ?: "",
                                    currentUsername = currentUser?.displayName ?: "",
                                    onLike = { targetPost ->
                                        viewModel.toggleLike(targetPost.id, currentUser?.uid ?: "")
                                    },
                                    onComment = { targetPost ->
                                        navController.navigate("${MainNavTarget.PostDetailScreen.route}/${targetPost.id}")
                                    },
                                    onSave = { targetPost ->
                                        viewModel.toggleSave(targetPost.id, currentUser?.uid ?: "")
                                    },
                                    modifier = Modifier.padding(vertical = 8.dp)
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
fun SavedPostItem(
    post: Post,
    currentUserId: String,
    currentUsername: String = "",
    onLike: (Post) -> Unit,
    onComment: (Post) -> Unit,
    onSave: (Post) -> Unit,
    onRepost: (Post) -> Unit = {},
    modifier: Modifier = Modifier
) {
    ListItem(
        post = post,
        currentUserId = currentUserId,
        currentUsername = currentUsername,
        onLike = onLike,
        onComment = onComment,
        onSave = onSave,
        onRepost = onRepost,
        modifier = modifier
    )
}


private fun formatTimeAgo(timestamp: Long): String {
    val currentTime = System.currentTimeMillis()
    val diff = currentTime - timestamp
    
    return when {
        diff < 60000 -> "Az önce"
        diff < 3600000 -> "${diff / 60000} dakika önce"
        diff < 86400000 -> "${diff / 3600000} saat önce"
        diff < 2592000000 -> "${diff / 86400000} gün önce"
        else -> "${diff / 2592000000} ay önce"
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SavedPostsScreenPreview() {
    // Preview için dummy data
}
