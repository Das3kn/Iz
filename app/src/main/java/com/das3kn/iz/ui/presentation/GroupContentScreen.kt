package com.das3kn.iz.ui.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.das3kn.iz.data.model.Post
import com.das3kn.iz.ui.presentation.groups.GroupMockData
import com.das3kn.iz.ui.presentation.groups.GroupDetailViewModel
import com.das3kn.iz.ui.presentation.groups.GroupUiModel
import com.das3kn.iz.ui.presentation.groups.GroupUserUiModel
import com.das3kn.iz.ui.presentation.groups.GroupSettingsDialog
import com.das3kn.iz.ui.presentation.home.components.ListItem
import com.das3kn.iz.ui.presentation.navigation.MainNavTarget

private enum class GroupDetailTab(val label: String) {
    POSTS("Paylaşımlar"),
    MEMBERS("Üyeler"),
    ABOUT("Hakkında")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsContentScreen(
    groupId: String,
    initialIsJoined: Boolean,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: GroupDetailViewModel = hiltViewModel()
) {
    val mockDetail = remember(groupId) { GroupMockData.groupDetail(groupId) }
    val currentUser = remember { GroupMockData.currentUser }

    var groupState by remember(groupId) {
        mutableStateOf<GroupUiModel?>(mockDetail?.group?.copy(isJoined = initialIsJoined))
    }
    val posts = remember(groupId) {
        mutableStateListOf<Post>().apply { mockDetail?.posts?.let { addAll(it) } }
    }
    val members = remember(groupId) { mockDetail?.members.orEmpty() }
    var selectedTab by remember { mutableStateOf(GroupDetailTab.POSTS) }

    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(groupId) {
        viewModel.loadGroup(groupId)
    }

    LaunchedEffect(uiState.group) {
        uiState.group?.let { updated ->
            groupState = updated
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.navigateBackAfterDelete) {
        if (uiState.navigateBackAfterDelete) {
            navController.popBackStack()
            viewModel.consumeNavigationEvent()
        }
    }

    // ✅ Küçük app bar + enterAlways (aşağı kaydırınca gizlenir, yukarı kaydırınca görünür)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    val group = groupState

    if (group == null) {
        GroupDetailPlaceholder(
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage,
            onBack = { navController.popBackStack() }
        )
    } else {
        Scaffold(
            topBar = {
                GroupDetailTopBar(
                    group = group,
                    onBack = { navController.popBackStack() },
                    isAdmin = uiState.isAdmin || group.admin.id == currentUser.id,
                    onOpenSettings = { viewModel.openSettings(force = group.admin.id == currentUser.id) },
                    scrollBehavior = scrollBehavior
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF3F4F6))
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                item {
                    GroupDetailHeader(
                        group = group,
                        isJoined = group.isJoined,
                        onToggleJoin = {
                            groupState = groupState?.let { current ->
                                val joined = !current.isJoined
                                current.copy(
                                    isJoined = joined,
                                    membersCount = (current.membersCount + if (joined) 1 else -1).coerceAtLeast(0)
                                )
                            }
                        },
                        onCreatePost = { navController.navigate(MainNavTarget.CreatePostScreen.route) }
                    )
                }

                item {
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = MaterialTheme.colorScheme.surface
                    ) {
                        GroupDetailTab.values().forEach { tab ->
                            Tab(
                                selected = selectedTab == tab,
                                onClick = { selectedTab = tab },
                                text = { Text(text = tab.label) }
                            )
                        }
                    }
                }

                when (selectedTab) {
                    GroupDetailTab.POSTS -> {
                        if (!group.isJoined) {
                            item {
                                LockedContentMessage(onJoinClick = {
                                    groupState = groupState?.let { current ->
                                        current.copy(
                                            isJoined = true,
                                            membersCount = (current.membersCount + 1).coerceAtLeast(1)
                                        )
                                    }
                                })
                            }
                        } else if (posts.isEmpty()) {
                            item {
                                EmptyPostsState(onCreatePost = {
                                    navController.navigate(MainNavTarget.CreatePostScreen.route)
                                })
                            }
                        } else {
                            items(posts, key = { it.id }) { post ->
                                ListItem(
                                    post = post,
                                    currentUserId = currentUser.id,
                                    onLike = { toggleLike(posts, it.id, currentUser.id) },
                                    onComment = {
                                        navController.navigate("${MainNavTarget.CommentsScreen.route}/${it.id}")
                                    },
                                    onSave = { toggleSave(posts, it.id, currentUser.id) },
                                    onRepost = { toggleRepost(posts, it.id, currentUser) },
                                    onProfileClick = { userId ->
                                        if (userId.isNotBlank()) {
                                            navController.navigate("${MainNavTarget.ProfileScreen.route}/$userId")
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.surface)
                                )
                            }
                        }
                    }

                    GroupDetailTab.MEMBERS -> {
                        when {
                            !group.isJoined -> item { LockedMembersMessage() }
                            members.isEmpty() -> item { EmptyMembersState() }
                            else -> {
                                items(members, key = { it.id }) { member ->
                                    MemberRow(
                                        member = member,
                                        isAdmin = member.id == group.admin.id,
                                        onClick = {
                                            navController.navigate("${MainNavTarget.ProfileScreen.route}/${member.id}")
                                        }
                                    )
                                }
                            }
                        }
                    }

                    GroupDetailTab.ABOUT -> {
                        item {
                            AboutSection(
                                group = group,
                                onNavigateToAdmin = {
                                    navController.navigate("${MainNavTarget.ProfileScreen.route}/${group.admin.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    val settingsGroup = uiState.group
    if (uiState.isSettingsOpen && settingsGroup != null) {
        GroupSettingsDialog(
            group = settingsGroup,
            members = members,
            isAdmin = uiState.isAdmin,
            isSaving = uiState.isSaving,
            isDeleting = uiState.deleteInProgress,
            onDismiss = { viewModel.closeSettings() },
            onSave = { settings -> viewModel.saveSettings(settings) },
            onDelete = { viewModel.deleteGroup() }
        )
    }
}

@Composable
private fun GroupDetailPlaceholder(
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit
) {
    val showLoading = isLoading || errorMessage == null
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (showLoading) {
                CircularProgressIndicator()
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = errorMessage ?: "Grup bulunamadı",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                    OutlinedButton(onClick = onBack) {
                        Text(text = "Geri dön")
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMembersState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.People,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = "Henüz üye yok",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            text = "Üyeler burada görünecek.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupDetailTopBar(
    group: GroupUiModel,
    onBack: () -> Unit,
    isAdmin: Boolean,
    onOpenSettings: () -> Unit,
    scrollBehavior: androidx.compose.material3.TopAppBarScrollBehavior
) {
    TopAppBar( // 👈 küçük top bar
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Geri")
            }
        },
        title = {
            Column {
                Text(text = group.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${group.membersCount} üye",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            if (isAdmin) {
                IconButton(onClick = onOpenSettings) {
                    Icon(imageVector = Icons.Filled.Settings, contentDescription = "Ayarlar")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface // istersen farklı ton verebilirsin
        ),
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupDetailHeader(
    group: GroupUiModel,
    isJoined: Boolean,
    onToggleJoin: () -> Unit,
    onCreatePost: () -> Unit
) {
    // Boyutlar
    val coverHeight = 200.dp
    val avatarSize = 120.dp
    val horizontalPadding = 20.dp

    // Header yüksekliği = kapak + avatarın yarısı (taşma)
    val headerHeight = coverHeight + (avatarSize / 2)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight)
        ) {
            // Kapak: kenardan kenara
            AsyncImage(
                model = group.imageUrl,
                contentDescription = group.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(coverHeight)
                    .align(Alignment.TopCenter)
            )

            // Kapak üstü gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(coverHeight)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                        )
                    )
            )

            // Padding'li overlay: kırpılma yok
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding)
            ) {
                // Avatar — alt-sola
                AsyncImage(
                    model = group.imageUrl,
                    contentDescription = group.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(avatarSize)
                        .clip(RoundedCornerShape(32.dp))
                        .align(Alignment.BottomStart)
                )

                // Joined ise sağ altta aksiyonlar
                if (isJoined) {
                    Row(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledIconButton(
                            onClick = { /* todo: davet */ },
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Icon(imageVector = Icons.Outlined.PersonAdd, contentDescription = "Üye ekle")
                        }
                        FilledIconButton(
                            onClick = onCreatePost,
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFF7C3AED),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Filled.Add, contentDescription = "Paylaşım oluştur")
                        }
                    }
                }
            }
        }

        // Başlık aralığı
        Spacer(modifier = Modifier.height(6.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = horizontalPadding)
        ) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = group.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                InfoPill(icon = Icons.Outlined.People, label = "${group.membersCount} üye")
                InfoPill(icon = Icons.Outlined.Article, label = "${group.postsCount} paylaşım")
            }
            Spacer(modifier = Modifier.height(22.dp))

            if (isJoined) {
                OutlinedButton(
                    onClick = onToggleJoin,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text(text = "Gruptan Ayrıl")
                }
            } else {
                Button(
                    onClick = onToggleJoin,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C3AED),
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Gruba Katıl")
                }
            }
        }
    }
}

@Composable
private fun InfoPill(icon: ImageVector, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF3F4F6))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LockedContentMessage(onJoinClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Grup içeriğini görmek için katılın",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Bu grubun paylaşımlarını görmek için gruba katılmalısınız",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onJoinClick,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7C3AED),
                contentColor = Color.White
            )
        ) {
            Text(text = "Gruba Katıl")
        }
    }
}

@Composable
private fun EmptyPostsState(onCreatePost: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Henüz paylaşım yok", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "İlk paylaşımı yapmaya ne dersin?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onCreatePost,
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF7C3AED),
                contentColor = Color.White
            )
        ) {
            Text(text = "Paylaşım Oluştur")
        }
    }
}

@Composable
private fun LockedMembersMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Üyeleri görmek için gruba katılmalısınız",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MemberRow(
    member: GroupUserUiModel,
    isAdmin: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = member.avatarUrl,
            contentDescription = member.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(text = member.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                text = "@${member.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isAdmin) {
            Text(
                text = "Yönetici",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF7C3AED),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEDE9FE))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun AboutSection(
    group: GroupUiModel,
    onNavigateToAdmin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(20.dp)
    ) {
        Text(text = "Açıklama", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = group.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Grup Bilgileri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))
        InfoPill(icon = Icons.Outlined.People, label = "${group.membersCount} üye")
        Spacer(modifier = Modifier.height(8.dp))
        InfoPill(icon = Icons.Outlined.Article, label = "${group.postsCount} paylaşım")
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Yönetici", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF9FAFB))
                .clickable { onNavigateToAdmin() }
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = group.admin.avatarUrl,
                contentDescription = group.admin.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
            )
            Column {
                Text(text = group.admin.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "@${group.admin.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun toggleLike(posts: MutableList<Post>, postId: String, userId: String) {
    val index = posts.indexOfFirst { it.id == postId }
    if (index >= 0) {
        val current = posts[index]
        val hasLiked = current.likes.contains(userId)
        val updatedLikes = if (hasLiked) current.likes.filterNot { it == userId } else current.likes + userId
        posts[index] = current.copy(likes = updatedLikes)
    }
}

private fun toggleSave(posts: MutableList<Post>, postId: String, userId: String) {
    val index = posts.indexOfFirst { it.id == postId }
    if (index >= 0) {
        val current = posts[index]
        val hasSaved = current.saves.contains(userId)
        val updatedSaves = if (hasSaved) current.saves.filterNot { it == userId } else current.saves + userId
        posts[index] = current.copy(saves = updatedSaves)
    }
}

private fun toggleRepost(posts: MutableList<Post>, postId: String, currentUser: GroupUserUiModel) {
    val index = posts.indexOfFirst { it.id == postId }
    if (index >= 0) {
        val current = posts[index]
        val hasReposted = current.repostedByUserId == currentUser.id
        val newCount = if (hasReposted) (current.shares - 1).coerceAtLeast(0) else current.shares + 1
        posts[index] = current.copy(
            shares = newCount,
            repostedByUserId = if (hasReposted) null else currentUser.id,
            repostedByDisplayName = if (hasReposted) null else currentUser.name,
            repostedByUsername = if (hasReposted) null else currentUser.username
        )
    }
}
