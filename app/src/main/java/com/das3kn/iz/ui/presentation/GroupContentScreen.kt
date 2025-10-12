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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.das3kn.iz.data.model.MediaType
import com.das3kn.iz.data.model.Post
import com.das3kn.iz.ui.presentation.home.components.ListItem
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsContentScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val scrollState = rememberScrollState()

    val admin = remember {
        GroupDetailUser(
            id = "1",
            name = "Elif Kaya",
            username = "elifkaya",
            avatarUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400&h=400&fit=crop"
        )
    }

    var group by remember {
        mutableStateOf(
            GroupDetailUiModel(
                id = "group-1",
                name = "Yapay Zeka Topluluğu",
                description = "Yapay zeka ve makine öğrenimi üzerine en yeni gelişmeleri, projeleri ve ilham verici başarı hikayelerini paylaştığımız topluluk.",
                imageUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=1600&h=900&fit=crop",
                membersCount = 1254,
                postsCount = 328,
                isJoined = true,
                admin = admin
            )
        )
    }

    val members = remember {
        listOf(
            admin,
            GroupDetailUser(
                id = "2",
                name = "Ayşe Demir",
                username = "aysedemir",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&h=400&fit=crop"
            ),
            GroupDetailUser(
                id = "3",
                name = "Ahmet Yılmaz",
                username = "ahmetyilmaz",
                avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=400&h=400&fit=crop"
            ),
            GroupDetailUser(
                id = "4",
                name = "Mehmet Kaya",
                username = "mehmetkaya",
                avatarUrl = "https://images.unsplash.com/photo-1599566150163-29194dcaad36?w=400&h=400&fit=crop"
            )
        )
    }

    val currentUserId = remember { "current-user" }
    val posts = remember {
        mutableStateListOf(
            Post(
                id = "post-1",
                userId = members[1].id,
                username = members[1].name,
                userProfileImage = members[1].avatarUrl,
                content = "Yeni AI teknolojileri hakkında ne düşünüyorsunuz?",
                createdAt = System.currentTimeMillis() - 30 * 60 * 1000,
                likes = List(45) { "user-${it + 1}" },
                commentCount = 12,
                shares = 3
            ),
            Post(
                id = "post-2",
                userId = members[2].id,
                username = members[2].name,
                userProfileImage = members[2].avatarUrl,
                content = "Bugün yeni bir proje başlattım, heyecanlıyım! 🚀",
                mediaUrls = listOf("https://images.unsplash.com/photo-1498050108023-c5249f4df085?w=800&h=600&fit=crop"),
                mediaType = MediaType.IMAGE,
                createdAt = System.currentTimeMillis() - 2 * 60 * 60 * 1000,
                likes = List(78) { "user-${it + 51}" },
                commentCount = 23,
                shares = 15,
                repostedByUserId = currentUserId,
                repostedByDisplayName = "Sen"
            )
        )
    }

    var selectedTab by remember { mutableStateOf(GroupDetailTab.POSTS) }
    var selectedPostId by remember { mutableStateOf<String?>(null) }
    var isCommentsOpen by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(isCommentsOpen) {
        if (!isCommentsOpen) {
            sheetState.hide()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(tonalElevation = 2.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = group.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = "${group.membersCount} üye",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .background(Color(0xFFF9FAFB))
        ) {
            Box {
                AsyncImage(
                    model = group.imageUrl,
                    contentDescription = group.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)
                            )
                        )
                )
            }

            Column(modifier = Modifier.background(Color.White)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .offset(y = (-48).dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        AsyncImage(
                            model = group.imageUrl,
                            contentDescription = group.name,
                            modifier = Modifier
                                .size(120.dp)
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color.White),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Spacer(modifier = Modifier.weight(1f))
                        if (group.isJoined) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = {
                                        group = group.copy(
                                            isJoined = false,
                                            membersCount = (group.membersCount - 1).coerceAtLeast(0)
                                        )
                                    },
                                    shape = RoundedCornerShape(24.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE5E7EB),
                                        contentColor = Color(0xFF111827)
                                    )
                                ) {
                                    Text(text = "Gruptan Ayrıl")
                                }

                                Button(
                                    onClick = { /* TODO: navigate to create post */ },
                                    modifier = Modifier.size(52.dp),
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF7C3AED),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Text(text = "+")
                                }
                            }
                        }
                    }
                }

                Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                    Spacer(modifier = Modifier.height(4.dp))
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
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatItem(icon = Icons.Outlined.People, label = "Üye", value = group.membersCount)
                        StatItem(icon = Icons.Outlined.ChatBubbleOutline, label = "Paylaşım", value = group.postsCount)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (!group.isJoined) {
                        Button(
                            onClick = {
                                group = group.copy(
                                    isJoined = true,
                                    membersCount = group.membersCount + 1
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7C3AED),
                                contentColor = Color.White
                            )
                        ) {
                            Text(text = "Gruba Katıl")
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            Surface(tonalElevation = 1.dp) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    TabRow(selectedTabIndex = selectedTab.ordinal) {
                        GroupDetailTab.values().forEachIndexed { index, tab ->
                            Tab(
                                selected = selectedTab.ordinal == index,
                                onClick = { selectedTab = tab },
                                text = { Text(text = tab.title) }
                            )
                        }
                    }

                    when (selectedTab) {
                        GroupDetailTab.POSTS -> {
                            if (!group.isJoined) {
                                LockedContent(onJoinClick = {
                                    group = group.copy(
                                        isJoined = true,
                                        membersCount = group.membersCount + 1
                                    )
                                })
                            } else if (posts.isEmpty()) {
                                Surface(color = Color.White) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 24.dp, vertical = 40.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        Text(
                                            text = "Henüz paylaşım yok",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = "İlk paylaşımı yaparak topluluğu hareketlendirin",
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Button(
                                            onClick = { /* TODO: open create post */ },
                                            shape = RoundedCornerShape(24.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF7C3AED),
                                                contentColor = Color.White
                                            )
                                        ) {
                                            Text(text = "İlk Paylaşımı Yap")
                                        }
                                    }
                                }
                            } else {
                                Surface(color = Color.White) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        posts.forEach { post ->
                                            ListItem(
                                                post = post,
                                                currentUserId = currentUserId,
                                                onLike = { toggledPost ->
                                                    val index = posts.indexOfFirst { it.id == toggledPost.id }
                                                    if (index >= 0) {
                                                        val current = posts[index]
                                                        val hasLiked = current.likes.contains(currentUserId)
                                                        val updatedLikes = if (hasLiked) {
                                                            current.likes.filterNot { it == currentUserId }
                                                        } else {
                                                            current.likes + currentUserId
                                                        }
                                                        posts[index] = current.copy(likes = updatedLikes)
                                                    }
                                                },
                                                onComment = { toggledPost ->
                                                    selectedPostId = toggledPost.id
                                                    isCommentsOpen = true
                                                    coroutineScope.launch { sheetState.show() }
                                                },
                                                onRepost = { toggledPost ->
                                                    val index = posts.indexOfFirst { it.id == toggledPost.id }
                                                    if (index >= 0) {
                                                        val current = posts[index]
                                                        val hasReposted = current.repostedByUserId == currentUserId
                                                        posts[index] = current.copy(
                                                            shares = if (hasReposted) (current.shares - 1).coerceAtLeast(0) else current.shares + 1,
                                                            repostedByUserId = if (hasReposted) null else currentUserId,
                                                            repostedByUsername = if (hasReposted) null else "currentuser",
                                                            repostedByDisplayName = if (hasReposted) null else "Siz"
                                                        )
                                                    }
                                                },
                                                onSave = {},
                                                onProfileClick = {}
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        GroupDetailTab.MEMBERS -> {
                            if (!group.isJoined) {
                                LockedContent(onJoinClick = {
                                    group = group.copy(
                                        isJoined = true,
                                        membersCount = group.membersCount + 1
                                    )
                                })
                            } else {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.White)
                                ) {
                                    members.forEach { member ->
                                        MemberRow(member = member, isAdmin = member.id == group.admin.id)
                                        Divider(color = Color(0xFFE5E7EB))
                                    }
                                }
                            }
                        }

                        GroupDetailTab.ABOUT -> {
                            AboutSection(group = group, admin = group.admin)
                        }
                    }
                }
            }
        }
    }

    if (isCommentsOpen && selectedPostId != null) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { isCommentsOpen = false }
        ) {
            CommentsContent(postId = selectedPostId!!)
        }
    }
}

@Composable
private fun StatItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: Int) {
    Column(horizontalAlignment = Alignment.Start) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$value",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LockedContent(onJoinClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.People,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Grup içeriğini görmek için katılın",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Bu grubun paylaşımlarını görmek için gruba katılmalısınız",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Button(
            onClick = onJoinClick,
            shape = RoundedCornerShape(24.dp),
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
private fun MemberRow(member: GroupDetailUser, isAdmin: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = member.avatarUrl,
            contentDescription = member.name,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "@${member.username}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (isAdmin) {
            Text(
                text = "Yönetici",
                color = Color(0xFF7C3AED),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .background(Color(0xFFEDE9FE), RoundedCornerShape(16.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun AboutSection(group: GroupDetailUiModel, admin: GroupDetailUser) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Açıklama",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = group.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Grup Bilgileri",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(imageVector = Icons.Outlined.People, contentDescription = null)
                Text(
                    text = "${group.membersCount} üye",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(imageVector = Icons.Outlined.ChatBubbleOutline, contentDescription = null)
                Text(
                    text = "${group.postsCount} paylaşım",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "Yönetici",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            MemberRow(member = admin, isAdmin = true)
        }
    }
}

@Composable
@Composable
private fun CommentsContent(postId: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Yorumlar",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        repeat(3) { index ->
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Kullanıcı ${index + 1}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "Post ($postId) için örnek yorum ${index + 1}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Divider(color = Color(0xFFE5E7EB))
            }
        }
    }
}

private data class GroupDetailUiModel(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val membersCount: Int,
    val postsCount: Int,
    val isJoined: Boolean,
    val admin: GroupDetailUser
)

private data class GroupDetailUser(
    val id: String,
    val name: String,
    val username: String,
    val avatarUrl: String
)

private enum class GroupDetailTab(val title: String) {
    POSTS("Paylaşımlar"),
    MEMBERS("Üyeler"),
    ABOUT("Hakkında")
}

private fun Long.relativeTimeString(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    val minutes = diff / (60 * 1000)
    val hours = diff / (60 * 60 * 1000)
    val days = diff / (24 * 60 * 60 * 1000)
    return when {
        minutes < 1 -> "az önce"
        minutes < 60 -> "$minutes dk"
        hours < 24 -> "$hours sa"
        else -> "$days g"
    }
}

