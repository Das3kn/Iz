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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.sharp.AccountBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
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
import com.das3kn.iz.NavigationItem
import com.das3kn.iz.ui.presentation.auth.AuthViewModel
import com.das3kn.iz.ui.presentation.auth.AuthState
import com.das3kn.iz.ui.presentation.home.components.ListItem
import com.das3kn.iz.ui.presentation.navigation.MainNavTarget
import com.das3kn.iz.ui.theme.components.LoginCard
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
        var selectedItemIndex by rememberSaveable {
            mutableStateOf(0)
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
                ModalDrawerSheet {
                    Spacer(modifier = Modifier.height(16.dp))

                    Box {
                        Column {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxHeight(0.4f)
                                    .fillMaxWidth()
                            ) {
                                // Header decoration
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Iz",
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth()
                            ) {}
                        }

                        Column {
                            if (isUserLoggedIn) {
                                // Kullanıcı giriş yapmışsa profil bilgilerini göster
                                UserProfileSection(
                                    currentUser = currentUser,
                                    userProfile = userProfile,
                                    isLoadingProfile = isLoadingProfile,
                                    onSignOut = {
                                        authViewModel.signOut()
                                        scope.launch {
                                            drawerState.close()
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(
                                            horizontal = 14.dp,
                                            vertical = 48.dp
                                        )
                                        .padding(top = 62.dp)
                                )
                            } else {
                                // Kullanıcı giriş yapmamışsa login card'ı göster
                                LoginCard(
                                    modifier = Modifier
                                        .padding(
                                            horizontal = 14.dp,
                                            vertical = 48.dp
                                        )
                                        .padding(top = 62.dp)
                                )
                            }

                            if (isUserLoggedIn) {
                                // Giriş yapmış kullanıcı için menü öğeleri
                                menuItems.forEachIndexed { index, item ->
                                    NavigationDrawerItem(
                                        label = {
                                            Text(text = item.title)
                                        },
                                        selected = index == selectedItemIndex,
                                        onClick = {
                                            when (item.route) {
                                                "logout" -> {
                                                    // Oturum kapat
                                                    authViewModel.signOut()
                                                    scope.launch {
                                                        drawerState.close()
                                                    }
                                                }
                                                else -> {
                                                    // Normal navigation
                                                    navController.navigate(item.route)
                                                    selectedItemIndex = index
                                                    scope.launch {
                                                        drawerState.close()
                                                    }
                                                }
                                            }
                                        },
                                        icon = {
                                            Icon(
                                                imageVector = if (index == selectedItemIndex) {
                                                    item.selectedIcon
                                                } else item.unselectedIcon,
                                                contentDescription = item.title
                                            )
                                        },
                                        badge = {
                                            item.badgeCount?.let {
                                                Text(text = it.toString())
                                            }
                                        },
                                        modifier = Modifier
                                            .padding(NavigationDrawerItemDefaults.ItemPadding)
                                    )
                                }
                            }
                        }
                    }
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
                                                            navController.navigate("${MainNavTarget.PostDetailScreen.route}/${targetPost.id}")
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

@Composable
fun UserProfileSection(
    currentUser: com.google.firebase.auth.FirebaseUser?,
    userProfile: User?,
    isLoadingProfile: Boolean,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        // Profil resmi placeholder'ı
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(
                    MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            if (isLoadingProfile) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = Color.White
                )
            } else {
                Text(
                    text = userProfile?.displayName?.firstOrNull()?.uppercase()
                        ?: currentUser?.displayName?.firstOrNull()?.uppercase()
                        ?: "U",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (isLoadingProfile) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                text = userProfile?.displayName
                    ?: userProfile?.username
                    ?: currentUser?.displayName 
                    ?: "Kullanıcı",
                style = MaterialTheme.typography.titleMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }
        
        Text(
            text = currentUser?.email ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        androidx.compose.material3.OutlinedButton(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Filled.ExitToApp,
                contentDescription = "Çıkış Yap",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("Çıkış Yap")
        }
    }
}

val menuItems = listOf(
    NavigationItem(
        title = "Ana Sayfa",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Filled.Home,
        route = MainNavTarget.HomeScreen.route
    ),
    NavigationItem(
        title = "Sohbetler",
        selectedIcon = Icons.Filled.Email,
        unselectedIcon = Icons.Filled.Email,
        route = MainNavTarget.ChatListScreen.route
    ),
    NavigationItem(
        title = "Gruplar",
        selectedIcon = Icons.Sharp.AccountBox,
        unselectedIcon = Icons.Sharp.AccountBox,
        route = MainNavTarget.GroupsScreen.route
    ),
    NavigationItem(
        title = "Bloglar",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        route = MainNavTarget.BlogsScreen.route
    ),
    NavigationItem(
        title = "Profil",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        route = MainNavTarget.ProfileScreen.route
    ),
    NavigationItem(
        title = "Oturum Kapat",
        selectedIcon = Icons.Filled.ExitToApp,
        unselectedIcon = Icons.Filled.ExitToApp,
        route = "logout" // Özel route
    ),
)

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomeScreenPreview() {
    //HomeScreen(navController)
}