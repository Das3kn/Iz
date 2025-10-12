package com.das3kn.iz.ui.presentation.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.das3kn.iz.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.width
import com.das3kn.iz.ui.presentation.navigation.MainNavTarget

@Suppress("UnusedParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val currentUser = remember {
        GroupUserUiModel(
            id = "1",
            name = "Elif Kaya",
            username = "elifkaya",
            avatarUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400&h=400&fit=crop"
        )
    }

    var groups by remember { mutableStateOf(initialGroups(currentUser)) }
    var isCreateGroupOpen by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf<GroupUiModel?>(null) }
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var groupImageUrl by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }
    var isCreatePostOpen by remember { mutableStateOf(false) }

    val groupPosts = remember {
        mutableStateMapOf(
            groups[0].id to mutableStateListOf(
                GroupPostUiModel(
                    id = "g1",
                    groupId = groups[0].id,
                    author = GroupUserUiModel(
                        id = "2",
                        name = "Ayşe Demir",
                        username = "aysedemir",
                        avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=400&h=400&fit=crop"
                    ),
                    content = "Yeni AI teknolojileri hakkında ne düşünüyorsunuz?",
                    timestamp = System.currentTimeMillis() - 30 * 60 * 1000,
                    likes = 45,
                    comments = 12,
                    reposts = 3
                )
            )
        )
    }

    LaunchedEffect(groups.size) {
        groups.forEach { group ->
            if (!groupPosts.containsKey(group.id)) {
                groupPosts[group.id] = mutableStateListOf()
            }
        }
    }

    val filteredGroups = remember(groups, searchQuery) {
        groups.filter { group ->
            group.name.contains(searchQuery, ignoreCase = true) ||
                group.description.contains(searchQuery, ignoreCase = true)
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 2.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gruplar",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.weight(1f)
                        )
                        FilledIconButton(
                            onClick = { isCreateGroupOpen = true },
                            shape = CircleShape,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = Color(0xFF7C3AED),
                                contentColor = Color.White
                            )
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Yeni grup oluştur")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(text = "Grup ara...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { /* hide keyboard if needed */ })
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF3F4F6)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredGroups, key = { it.id }) { group ->
                    GroupCard(
                        group = group,
                        onSelect = { selectedGroup = group },
                        onToggleJoin = { toggledGroup ->
                            groups = groups.map {
                                if (it.id == toggledGroup.id) {
                                    val joined = !it.isJoined
                                    it.copy(
                                        isJoined = joined,
                                        membersCount = (it.membersCount + if (joined) 1 else -1).coerceAtLeast(0)
                                    )
                                } else it
                            }
                        }
                    )
                }
            }
        }
    }

    if (isCreateGroupOpen) {
        CreateGroupDialog(
            groupName = groupName,
            groupDescription = groupDescription,
            groupImageUrl = groupImageUrl,
            onGroupNameChange = { groupName = it },
            onGroupDescriptionChange = { groupDescription = it },
            onGroupImageUrlChange = { groupImageUrl = it },
            onDismiss = { isCreateGroupOpen = false },
            onCreate = {
                val newGroup = GroupUiModel(
                    id = System.currentTimeMillis().toString(),
                    name = groupName.trim(),
                    description = groupDescription.trim(),
                    imageUrl = groupImageUrl.trim().ifBlank { DEFAULT_GROUP_IMAGE },
                    membersCount = 1,
                    postsCount = 0,
                    isJoined = true,
                    admin = currentUser
                )
                groups = listOf(newGroup) + groups
                groupPosts[newGroup.id] = mutableStateListOf()
                groupName = ""
                groupDescription = ""
                groupImageUrl = ""
                isCreateGroupOpen = false
            }
        )
    }

    val selectedGroupValue = selectedGroup
    if (selectedGroupValue != null) {
        val posts = groupPosts[selectedGroupValue.id] ?: emptyList()
        ModalBottomSheet(
            onDismissRequest = { selectedGroup = null },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            GroupDetailContent(
                group = selectedGroupValue,
                posts = posts,
                onToggleJoin = {
                    groups = groups.map {
                        if (it.id == selectedGroupValue.id) {
                            val joined = !it.isJoined
                            val updated = it.copy(
                                isJoined = joined,
                                membersCount = (it.membersCount + if (joined) 1 else -1).coerceAtLeast(0)
                            )
                            selectedGroup = updated
                            updated
                        } else it
                    }
                },
                onCreatePost = { isCreatePostOpen = true },
                onLike = { postId ->
                    val currentPosts = groupPosts[selectedGroupValue.id] ?: return@GroupDetailContent
                    val index = currentPosts.indexOfFirst { it.id == postId }
                    if (index >= 0) {
                        val post = currentPosts[index]
                        currentPosts[index] = post.copy(
                            isLiked = !post.isLiked,
                            likes = if (!post.isLiked) post.likes + 1 else (post.likes - 1).coerceAtLeast(0)
                        )
                    }
                },
                onComment = { postId ->
                    navController.navigate("${MainNavTarget.CommentsScreen.route}/$postId")
                },
                onRepost = { postId ->
                    val currentPosts = groupPosts[selectedGroupValue.id] ?: return@GroupDetailContent
                    val index = currentPosts.indexOfFirst { it.id == postId }
                    if (index >= 0) {
                        val post = currentPosts[index]
                        currentPosts[index] = post.copy(
                            isReposted = !post.isReposted,
                            reposts = if (!post.isReposted) post.reposts + 1 else (post.reposts - 1).coerceAtLeast(0)
                        )
                    }
                }
            )
        }
    }

    if (isCreatePostOpen && selectedGroupValue != null) {
        CreatePostSheet(
            onDismiss = { isCreatePostOpen = false },
            onCreate = { content, imageUrl ->
                if (content.isNotBlank()) {
                    val newPost = GroupPostUiModel(
                        id = System.currentTimeMillis().toString(),
                        groupId = selectedGroupValue.id,
                        author = currentUser,
                        content = content.trim(),
                        imageUrl = imageUrl?.takeIf { it.isNotBlank() },
                        timestamp = System.currentTimeMillis()
                    )
                    val posts = groupPosts[selectedGroupValue.id]
                    if (posts != null) {
                        posts.add(0, newPost)
                    } else {
                        groupPosts[selectedGroupValue.id] = mutableStateListOf(newPost)
                    }
                    groups = groups.map {
                        if (it.id == selectedGroupValue.id) {
                            val updated = it.copy(postsCount = it.postsCount + 1)
                            selectedGroup = updated
                            updated
                        } else it
                    }
                }
                isCreatePostOpen = false
            }
        )
    }

}

@Composable
private fun GroupCard(
    group: GroupUiModel,
    onSelect: () -> Unit,
    onToggleJoin: (GroupUiModel) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            AsyncImage(
                model = group.imageUrl,
                contentDescription = group.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.worker_image),
                error = painterResource(id = R.drawable.worker_image)
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = group.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    StatPill(icon = Icons.Outlined.People, label = "${group.membersCount}")
                    StatPill(icon = Icons.Outlined.Article, label = "${group.postsCount}")
                }
                Spacer(modifier = Modifier.height(16.dp))
                val joined = group.isJoined
                Button(
                    onClick = { onToggleJoin(group) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = if (joined) {
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE5E7EB),
                            contentColor = Color(0xFF111827)
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF7C3AED),
                            contentColor = Color.White
                        )
                    }
                ) {
                    Text(text = if (joined) "Ayrıl" else "Katıl")
                }
            }
        }
    }
}

@Composable
private fun StatPill(icon: ImageVector, label: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF3F4F6))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun GroupDetailContent(
    group: GroupUiModel,
    posts: List<GroupPostUiModel>,
    onToggleJoin: () -> Unit,
    onCreatePost: () -> Unit,
    onLike: (String) -> Unit,
    onComment: (String) -> Unit,
    onRepost: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AsyncImage(
                model = group.imageUrl,
                contentDescription = group.name,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.worker_image),
                error = painterResource(id = R.drawable.worker_image)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.name,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = group.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatPill(icon = Icons.Outlined.People, label = "${group.membersCount} üye")
                    StatPill(icon = Icons.Outlined.Article, label = "${group.postsCount} paylaşım")
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onToggleJoin,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = if (group.isJoined) {
                ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE5E7EB),
                    contentColor = Color(0xFF111827)
                )
            } else {
                ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C3AED),
                    contentColor = Color.White
                )
            }
        ) {
            Text(text = if (group.isJoined) "Gruptan Ayrıl" else "Gruba Katıl")
        }

        if (group.isJoined) {
            Spacer(modifier = Modifier.height(16.dp))
            ElevatedButton(
                onClick = onCreatePost,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = Color(0xFF7C3AED),
                    contentColor = Color.White
                )
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Yeni paylaşım yap")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = if (group.isJoined) "Paylaşımlar" else "Grup içeriğini görmek için katılın",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        if (!group.isJoined) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Grup içeriğini görmek için gruba katılmalısınız.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Spacer(modifier = Modifier.height(16.dp))
            if (posts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Henüz paylaşım yok",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    posts.forEach { post ->
                        GroupPostCard(
                            post = post,
                            onLike = { onLike(post.id) },
                            onComment = { onComment(post.id) },
                            onRepost = { onRepost(post.id) }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GroupPostCard(
    post: GroupPostUiModel,
    onLike: () -> Unit,
    onComment: () -> Unit,
    onRepost: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = post.author.avatarUrl,
                    contentDescription = post.author.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape),
                    placeholder = painterResource(id = R.drawable.worker_image),
                    error = painterResource(id = R.drawable.worker_image)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = post.author.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "@${post.author.username} • ${post.timestamp.relativeTimeString()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            post.imageUrl?.let { imageUrl ->
                Spacer(modifier = Modifier.height(12.dp))
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.worker_image),
                    error = painterResource(id = R.drawable.worker_image)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Divider(color = Color(0xFFE5E7EB))
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PostActionButton(
                    icon = Icons.Outlined.FavoriteBorder,
                    label = post.likes.toString(),
                    isActive = post.isLiked,
                    onClick = onLike
                )
                PostActionButton(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    label = post.comments.toString(),
                    isActive = false,
                    onClick = onComment
                )
                PostActionButton(
                    icon = Icons.Outlined.Repeat,
                    label = post.reposts.toString(),
                    isActive = post.isReposted,
                    onClick = onRepost
                )
            }
        }
    }
}

@Composable
private fun PostActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val contentColor = if (isActive) Color(0xFF7C3AED) else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor
        )
    }
}

@Composable
private fun CreateGroupDialog(
    groupName: String,
    groupDescription: String,
    groupImageUrl: String,
    onGroupNameChange: (String) -> Unit,
    onGroupDescriptionChange: (String) -> Unit,
    onGroupImageUrlChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreate: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onCreate,
                enabled = groupName.isNotBlank()
            ) {
                Text(text = "Oluştur")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "İptal")
            }
        },
        title = { Text(text = "Yeni Grup Oluştur") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = groupName,
                    onValueChange = onGroupNameChange,
                    label = { Text(text = "Grup Adı") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = groupDescription,
                    onValueChange = onGroupDescriptionChange,
                    label = { Text(text = "Açıklama") },
                    minLines = 3
                )
                OutlinedTextField(
                    value = groupImageUrl,
                    onValueChange = onGroupImageUrlChange,
                    label = { Text(text = "Görsel URL (isteğe bağlı)") },
                    singleLine = true
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePostSheet(
    onDismiss: () -> Unit,
    onCreate: (String, String?) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Yeni Paylaşım",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text(text = "Paylaşım içeriği") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                colors = TextFieldDefaults.outlinedTextFieldColors()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = imageUrl,
                onValueChange = { imageUrl = it },
                label = { Text(text = "Görsel URL (isteğe bağlı)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { onCreate(content, imageUrl.takeIf { it.isNotBlank() }) },
                enabled = content.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7C3AED),
                    contentColor = Color.White
                )
            ) {
                Text(text = "Paylaş")
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private data class GroupUiModel(
    val id: String,
    val name: String,
    val description: String,
    val imageUrl: String,
    val membersCount: Int,
    val postsCount: Int,
    val isJoined: Boolean,
    val admin: GroupUserUiModel
)

private data class GroupUserUiModel(
    val id: String,
    val name: String,
    val username: String,
    val avatarUrl: String
)

private data class GroupPostUiModel(
    val id: String,
    val groupId: String,
    val author: GroupUserUiModel,
    val content: String,
    val imageUrl: String? = null,
    val timestamp: Long,
    val likes: Int = 0,
    val comments: Int = 0,
    val reposts: Int = 0,
    val isLiked: Boolean = false,
    val isReposted: Boolean = false
)

private data class CommentUiModel(
    val id: String,
    val user: GroupUserUiModel,
    val content: String,
    val timestamp: Long,
    val likes: Int,
    val isLiked: Boolean
)

private fun initialGroups(currentUser: GroupUserUiModel): List<GroupUiModel> = listOf(
    GroupUiModel(
        id = "tech-ai",
        name = "Yapay Zeka Tutkunları",
        description = "AI, ML ve veri bilimi meraklılarının bir araya geldiği topluluk.",
        imageUrl = "https://images.unsplash.com/photo-1517430816045-df4b7de11d1d?w=1200&h=600&fit=crop",
        membersCount = 128,
        postsCount = 42,
        isJoined = true,
        admin = currentUser
    ),
    GroupUiModel(
        id = "designers",
        name = "Ürün Tasarımcıları",
        description = "Tasarım trendleri, figma tüyoları ve portfolyo paylaşımları.",
        imageUrl = "https://images.unsplash.com/photo-1521737604893-d14cc237f11d?w=1200&h=600&fit=crop",
        membersCount = 86,
        postsCount = 28,
        isJoined = false,
        admin = GroupUserUiModel(
            id = "3",
            name = "Mert Yıldız",
            username = "mertyildiz",
            avatarUrl = "https://images.unsplash.com/photo-1519345182560-3f2917c472ef?w=400&h=400&fit=crop"
        )
    ),
    GroupUiModel(
        id = "mobile-dev",
        name = "Mobil Geliştiriciler",
        description = "Android, iOS ve cross-platform geliştirme üzerine sohbetler.",
        imageUrl = "https://images.unsplash.com/photo-1517433456452-f9633a875f6f?w=1200&h=600&fit=crop",
        membersCount = 210,
        postsCount = 56,
        isJoined = true,
        admin = GroupUserUiModel(
            id = "4",
            name = "Nazlı Aydın",
            username = "nazli.dev",
            avatarUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=400&h=400&fit=crop"
        )
    ),
    GroupUiModel(
        id = "startup-tr",
        name = "Startup Türkiye",
        description = "Girişimciler ve yatırımcılar için deneyim paylaşımı alanı.",
        imageUrl = "https://images.unsplash.com/photo-1520607162513-77705c0f0d4a?w=1200&h=600&fit=crop",
        membersCount = 340,
        postsCount = 75,
        isJoined = false,
        admin = GroupUserUiModel(
            id = "5",
            name = "Kerem Öz",
            username = "keremoz",
            avatarUrl = "https://images.unsplash.com/photo-1544723795-3fb6469f5b39?w=400&h=400&fit=crop"
        )
    )
)

private fun Long.relativeTimeString(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    val minutes = diff / (60 * 1000)
    val hours = diff / (60 * 60 * 1000)
    val days = diff / (24 * 60 * 60 * 1000)
    return when {
        minutes < 1 -> "Az önce"
        minutes < 60 -> "${minutes} dk"
        hours < 24 -> "${hours} sa"
        days < 7 -> "${days} gün"
        else -> "${days / 7} hf"
    }
}

private const val DEFAULT_GROUP_IMAGE = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=800&h=600&fit=crop"

