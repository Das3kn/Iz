package com.das3kn.iz.ui.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.das3kn.iz.ui.presentation.auth.AuthViewModel
import com.das3kn.iz.ui.presentation.auth.AuthState
import com.das3kn.iz.ui.presentation.home.components.AppSidebar
import com.das3kn.iz.ui.presentation.home.components.ListItem
import com.das3kn.iz.ui.presentation.home.components.SidebarDestination
import com.das3kn.iz.ui.presentation.navigation.MainNavTarget
import com.das3kn.iz.ui.presentation.home.HomeViewModel
import com.das3kn.iz.data.repository.AuthRepository
import com.das3kn.iz.data.model.User
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        var selectedSidebarDestination by rememberSaveable {
            mutableStateOf(SidebarDestination.Feed)
        }
        
        val authState by authViewModel.authState.collectAsState()
        val currentUser by authViewModel.currentUser.collectAsState()
        val isUserLoggedIn = currentUser != null
        
        val homeState by homeViewModel.uiState.collectAsState()
        
        // Kullanıcı profilini AuthViewModel'den al
        val userProfile by authViewModel.userProfile.collectAsState()
        val isLoadingProfile = userProfile == null && currentUser != null

        // Auth state değişikliklerini dinle
        LaunchedEffect(authState) {
            when (authState) {
                is AuthState.Success -> {
                    // Başarılı giriş/kayıt sonrası drawer'ı kapat
                    scope.launch {
                        drawerState.close()
                    }
                    // State'i sıfırla
                    authViewModel.resetAuthState()
                }
                else -> {}
            }
        }
        
        // Post yüklendikten sonra otomatik güncelleme
        LaunchedEffect(Unit) {
            // HomeScreen her açıldığında post'ları yeniden yükle
            homeViewModel.loadPosts()
        }

        ModalNavigationDrawer(
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.widthIn(min = 280.dp, max = 340.dp),
                    drawerContainerColor = MaterialTheme.colorScheme.background
                ) {
                    AppSidebar(
                        currentUser = currentUser,
                        userProfile = userProfile,
                        isLoadingProfile = isLoadingProfile,
                        selectedDestination = selectedSidebarDestination,
                        onNavigate = { destination ->
                            selectedSidebarDestination = destination
                            when (destination) {
                                SidebarDestination.Feed -> {
                                    navController.navigate(MainNavTarget.HomeScreen.route) {
                                        launchSingleTop = true
                                    }
                                }

                                SidebarDestination.Groups -> {
                                    navController.navigate(MainNavTarget.GroupsScreen.route) {
                                        launchSingleTop = true
                                    }
                                }

                                SidebarDestination.Chats -> {
                                    navController.navigate(MainNavTarget.ChatListScreen.route) {
                                        launchSingleTop = true
                                    }
                                }

                                SidebarDestination.Notifications -> {
                                    navController.navigate(MainNavTarget.NotificationsScreen.route) {
                                        launchSingleTop = true
                                    }
                                }

                                SidebarDestination.Profile -> {
                                    navController.navigate(MainNavTarget.ProfileScreen.route) {
                                        launchSingleTop = true
                                    }
                                }

                                SidebarDestination.Friends -> {
                                    navController.navigate(MainNavTarget.PeopleScreen.route) {
                                        launchSingleTop = true
                                    }
                                }

                                SidebarDestination.Saved -> {
                                    navController.navigate(MainNavTarget.SavedPostsScreen.route) {
                                        launchSingleTop = true
                                    }
                                }

                                SidebarDestination.Settings -> {
                                    navController.navigate(MainNavTarget.ProfileScreen.route) {
                                        launchSingleTop = true
                                    }
                                }

                                SidebarDestination.Help -> Unit
                            }
                        },
                        onLogout = {
                            authViewModel.signOut()
                            selectedSidebarDestination = SidebarDestination.Feed
                        },
                        onClose = {
                            scope.launch {
                                drawerState.close()
                            }
                        }
                    )
                }
            },
            drawerState = drawerState
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = "Iz")
                        },
                        actions = {
                            if (isUserLoggedIn) {
                                IconButton(onClick = { homeViewModel.toggleSearchBarVisibility() }) {
                                    val isVisible = homeState.isSearchBarVisible
                                    Icon(
                                        imageVector = if (isVisible) Icons.Filled.Close else Icons.Filled.Search,
                                        contentDescription = if (isVisible) "Aramayı kapat" else "Kullanıcı ara",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                IconButton(
                                    onClick = { navController.navigate(MainNavTarget.NotificationsScreen.route) }
                                ) {
                                    Icon(
                                        Icons.Filled.Notifications,
                                        contentDescription = "Bildirimler",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Refresh butonu
                                IconButton(
                                    onClick = { homeViewModel.refreshPosts() }
                                ) {
                                    Icon(
                                        Icons.Filled.Refresh,
                                        contentDescription = "Yenile",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                // Post paylaşma butonu
                                IconButton(
                                    onClick = { navController.navigate(MainNavTarget.CreatePostScreen.route) }
                                ) {
                                    Icon(
                                        Icons.Filled.Add,
                                        contentDescription = "Yeni Gönderi",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                // Profil butonu
                                Image(
                                    imageVector = Icons.Filled.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .padding(end = 4.dp)
                                        .size(24.dp)
                                        .clickable {
                                            navController.navigate(MainNavTarget.ProfileScreen.route)
                                        }
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch {
                                    drawerState.open()
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu"
                                )
                            }
                        }
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .padding(it)
                        .fillMaxSize()
                ) {
                    if (isUserLoggedIn) {
                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            AnimatedVisibility(
                                visible = homeState.isSearchBarVisible,
                                enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                                exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut()
                            ) {
                                Column {
                                    OutlinedTextField(
                                        value = homeState.searchQuery,
                                        onValueChange = { query -> homeViewModel.updateSearchQuery(query) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        placeholder = { Text("Kullanıcı ara") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = Icons.Filled.Search,
                                                contentDescription = null
                                            )
                                        },
                                        singleLine = true
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            if (homeState.isSearchBarVisible && homeState.searchQuery.isNotBlank()) {
                                SearchResultsSection(
                                    modifier = Modifier
                                        .weight(1f, fill = true)
                                        .padding(horizontal = 16.dp),
                                    query = homeState.searchQuery,
                                    isLoading = homeState.isSearchingUsers,
                                    results = homeState.searchResults,
                                    error = homeState.searchError,
                                    currentUserId = currentUser?.uid,
                                    onUserClick = { user ->
                                        homeViewModel.clearSearchResults()
                                        if (user.id == currentUser?.uid) {
                                            navController.navigate(MainNavTarget.ProfileScreen.route)
                                        } else {
                                            navController.navigate("${MainNavTarget.ProfileScreen.route}/${user.id}")
                                        }
                                    }
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .weight(1f, fill = true)
                                        .fillMaxWidth()
                                ) {
                                    when {
                                        homeState.isLoading && homeState.posts.isEmpty() -> {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .padding(32.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                CircularProgressIndicator()
                                            }
                                        }

                                        homeState.error != null && homeState.posts.isEmpty() -> {
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
                                                        text = "Hata: ${homeState.error}",
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    Button(onClick = { homeViewModel.refreshPosts() }) {
                                                        Text("Tekrar Dene")
                                                    }
                                                }
                                            }
                                        }

                                        homeState.posts.isEmpty() -> {
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
                                                        text = "Henüz gönderi yok",
                                                        style = MaterialTheme.typography.bodyLarge,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(modifier = Modifier.height(16.dp))
                                                    Text(
                                                        text = "İlk gönderiyi sen paylaş!",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }

                                        else -> {
                                            LazyColumn(
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                items(homeState.posts) { post ->
                                                    ListItem(
                                                        post = post,
                                                        currentUserId = currentUser?.uid ?: "",
                                                        onLike = { targetPost ->
                                                            homeViewModel.toggleLike(targetPost.id, currentUser?.uid ?: "")
                                                        },
                                                        onComment = { targetPost ->
                                                            navController.navigate("${MainNavTarget.CommentsScreen.route}/${targetPost.id}")
                                                        },
                                                        onSave = { targetPost ->
                                                            homeViewModel.toggleSave(targetPost.id, currentUser?.uid ?: "")
                                                        },
                                                        onRepost = { targetPost ->
                                                            currentUser?.let { user ->
                                                                homeViewModel.repostPost(
                                                                    targetPost,
                                                                    user.uid,
                                                                    userProfile
                                                                )
                                                            }
                                                        },
                                                        onProfileClick = { userId ->
                                                            if (userId == currentUser?.uid) {
                                                                navController.navigate(MainNavTarget.ProfileScreen.route)
                                                            } else {
                                                                navController.navigate("${MainNavTarget.ProfileScreen.route}/$userId")
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .padding(vertical = 8.dp)
                                                            .clickable {
                                                                navController.navigate("${MainNavTarget.PostDetailScreen.route}/${(post.originalPost ?: post).id}")
                                                            }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Giriş yapmamış kullanıcı için hoş geldin mesajı
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "İZ'e Hoş Geldiniz!",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Buluşlarınızı ve keşiflerinizi paylaşmak için giriş yapın",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
private fun SearchResultsSection(
    modifier: Modifier = Modifier,
    query: String,
    isLoading: Boolean,
    results: List<User>,
    error: String?,
    currentUserId: String?,
    onUserClick: (User) -> Unit
) {
    val filteredResults = results.filter { user ->
        user.id.isNotBlank() && user.id != currentUserId
    }

    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            text = "\"$query\" için sonuçlar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            filteredResults.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Eşleşen kullanıcı bulunamadı",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredResults) { user ->
                        UserSearchResultItem(
                            user = user,
                            onClick = { onUserClick(user) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UserSearchResultItem(
    user: User,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.displayName.firstOrNull()?.uppercase()
                    ?: user.username.firstOrNull()?.uppercase()
                    ?: "?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = user.displayName.ifBlank { user.username },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (user.username.isNotBlank()) {
                Text(
                    text = "@${user.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    //HomeScreen(navController)
}