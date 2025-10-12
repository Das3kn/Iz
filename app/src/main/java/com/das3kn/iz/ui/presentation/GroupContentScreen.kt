package com.das3kn.iz.ui.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.das3kn.iz.R
import com.das3kn.iz.data.model.Post
import com.das3kn.iz.ui.presentation.groups.GroupMockData
import com.das3kn.iz.ui.presentation.groups.GroupUiModel
import com.das3kn.iz.ui.presentation.groups.GroupUserUiModel
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
    modifier: Modifier = Modifier
) {
    val groupDetail = remember(groupId) { GroupMockData.groupDetail(groupId) }
    val currentUser = remember { GroupMockData.currentUser }

    if (groupDetail == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        Surface(modifier = modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = "Grup bulunamadı", style = MaterialTheme.typography.bodyLarge)
            }
        }
        return
    }

    var groupState by remember(groupId) {
        mutableStateOf(groupDetail.group.copy(isJoined = initialIsJoined))
    }
    val posts = remember(groupId) {
        mutableStateListOf<Post>().apply { addAll(groupDetail.posts) }
    }
    var selectedTab by remember { mutableStateOf(GroupDetailTab.POSTS) }

    Scaffold(
        topBar = {
            GroupDetailTopBar(
                group = groupState,
                onBack = { navController.popBackStack() },
                isAdmin = groupState.admin.id == currentUser.id
            )
        },
        modifier = modifier
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
                    group = groupState,
                    isJoined = groupState.isJoined,
                    onToggleJoin = {
                        val joined = !groupState.isJoined
                        groupState = groupState.copy(
                            isJoined = joined,
                            membersCount = (groupState.membersCount + if (joined) 1 else -1).coerceAtLeast(0)
                        )
                    },
                    onCreatePost = { navController.navigate(MainNavTarget.CreatePostScreen.route) },
                    onNavigateToAdmin = { userId ->
                        navController.navigate("${MainNavTarget.ProfileScreen.route}/$userId")
                    }
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
                    if (!groupState.isJoined) {
                        item { LockedContentMessage(onJoinClick = {
                            groupState = groupState.copy(
                                isJoined = true,
                                membersCount = groupState.membersCount + 1
                            )
                        }) }
                    } else if (posts.isEmpty()) {
                        item { EmptyPostsState(onCreatePost = {
                            navController.navigate(MainNavTarget.CreatePostScreen.route)
                        }) }
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
                                onRepost = {
                                    toggleRepost(posts, it.id, currentUser)
                                },
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
                    if (!groupState.isJoined) {
                        item { LockedMembersMessage() }
                    } else {
                        items(groupDetail.members, key = { it.id }) { member ->
                            MemberRow(
                                member = member,
                                isAdmin = member.id == groupState.admin.id,
                                onClick = {
                                    navController.navigate("${MainNavTarget.ProfileScreen.route}/${member.id}")
                                }
                            )
                        }
                    }
                }

                GroupDetailTab.ABOUT -> {
                    item {
                        AboutSection(
                            group = groupState,
                            onNavigateToAdmin = {
                                navController.navigate("${MainNavTarget.ProfileScreen.route}/${groupState.admin.id}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupDetailTopBar(
    group: GroupUiModel,
    onBack: () -> Unit,
    isAdmin: Boolean
) {
    TopAppBar(
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
                IconButton(onClick = { /* ayarlar */ }) {
                    Icon(imageVector = Icons.Filled.Settings, contentDescription = "Ayarlar")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun GroupDetailHeader(
    group: GroupUiModel,
    isJoined: Boolean,
    onToggleJoin: () -> Unit,
    onCreatePost: () -> Unit,
    onNavigateToAdmin: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            AsyncImage(
                model = group.imageUrl,
                contentDescription = group.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = group.imageUrl,
                    contentDescription = group.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .border(4.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(32.dp))
                        .align(Alignment.TopStart)
                        .offset(y = (-60).dp)
                )
                if (isJoined) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
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

            Spacer(modifier = Modifier.height(56.dp))
            Text(
                text = group.name,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = group.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                InfoPill(icon = Icons.Outlined.People, label = "${group.membersCount} üye")
                InfoPill(icon = Icons.Outlined.Article, label = "${group.postsCount} paylaşım")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onNavigateToAdmin(group.admin.id) }
            ) {
                AsyncImage(
                    model = group.admin.avatarUrl,
                    contentDescription = group.admin.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Yönetici", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = group.admin.name, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

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
        val updatedLikes = if (hasLiked) {
            current.likes.filterNot { it == userId }
        } else {
            current.likes + userId
        }
        posts[index] = current.copy(likes = updatedLikes)
    }
}

private fun toggleSave(posts: MutableList<Post>, postId: String, userId: String) {
    val index = posts.indexOfFirst { it.id == postId }
    if (index >= 0) {
        val current = posts[index]
        val hasSaved = current.saves.contains(userId)
        val updatedSaves = if (hasSaved) {
            current.saves.filterNot { it == userId }
        } else {
            current.saves + userId
        }
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
