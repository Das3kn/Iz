package com.das3kn.iz.ui.presentation.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.das3kn.iz.data.model.Group
import com.das3kn.iz.data.model.User
import com.das3kn.iz.ui.presentation.home.components.ListItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupContentScreen(
    navController: NavHostController,
    groupId: String,
    viewModel: GroupContentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(GroupDetailTab.Feed) }
    var newPostText by remember { mutableStateOf("") }
    var isInviteDialogVisible by remember { mutableStateOf(false) }
    var selectedInvitees by remember { mutableStateOf(setOf<String>()) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(groupId) {
        viewModel.refresh()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.group?.name ?: "Grup",
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.group == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Grup bulunamadı", style = MaterialTheme.typography.bodyLarge)
                }
            }

            else -> {
                val group = uiState.group
                if (group == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "Grup bulunamadı", style = MaterialTheme.typography.bodyLarge)
                    }
                    return@Scaffold
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    GroupHeader(
                        group = group,
                        isOwner = uiState.isOwner,
                        isMember = uiState.isMember,
                        memberCount = uiState.members.size,
                        pendingCount = group.pendingInvites.size,
                        onInviteClick = {
                            selectedInvitees = emptySet()
                            isInviteDialogVisible = true
                        }
                    )

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
                        GroupDetailTab.Feed -> {
                        GroupFeedSection(
                            isMember = uiState.isMember,
                            isPosting = uiState.isPosting,
                            posts = uiState.posts,
                            newPostText = newPostText,
                            onPostTextChange = { newPostText = it },
                            onCreatePost = {
                                viewModel.createTextPost(newPostText)
                                newPostText = ""
                                },
                            currentUserId = uiState.currentUserId
                        )
                        }

                        GroupDetailTab.Members -> {
                            MembersSection(members = uiState.members)
                        }
                    }
                }
            }
        }
    }

    if (isInviteDialogVisible) {
        InviteFriendsDialog(
            friends = uiState.availableFriends,
            selectedFriendIds = selectedInvitees,
            isSubmitting = uiState.isInviting,
            onToggle = { friendId ->
                selectedInvitees = if (selectedInvitees.contains(friendId)) {
                    selectedInvitees - friendId
                } else {
                    selectedInvitees + friendId
                }
            },
            onDismiss = {
                isInviteDialogVisible = false
                selectedInvitees = emptySet()
            },
            onInvite = {
                viewModel.sendInvites(selectedInvitees.toList())
                isInviteDialogVisible = false
                selectedInvitees = emptySet()
            }
        )
    }
}

private enum class GroupDetailTab(val title: String) {
    Feed("Paylaşımlar"),
    Members("Üyeler")
}

@Composable
private fun GroupHeader(
    group: Group,
    isOwner: Boolean,
    isMember: Boolean,
    memberCount: Int,
    pendingCount: Int,
    onInviteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (group.description.isNotBlank()) {
                Text(
                    text = group.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$memberCount üye",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (pendingCount > 0) {
                    Text(
                        text = "$pendingCount bekleyen davet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (isMember) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = onInviteClick, enabled = isOwner || isMember) {
                    Icon(imageVector = Icons.Filled.PersonAdd, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Arkadaş Davet Et")
                }
            }
        }
    }
}

@Composable
private fun GroupFeedSection(
    isMember: Boolean,
    isPosting: Boolean,
    posts: List<com.das3kn.iz.data.model.Post>,
    newPostText: String,
    onPostTextChange: (String) -> Unit,
    onCreatePost: () -> Unit,
    currentUserId: String?
) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (isMember) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Gruba yeni bir paylaşım yap",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = newPostText,
                        onValueChange = onPostTextChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        placeholder = { Text(text = "Paylaşımını yaz...") }
                    )
                    Button(
                        onClick = onCreatePost,
                        enabled = newPostText.isNotBlank() && !isPosting,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 12.dp)
                    ) {
                        if (isPosting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(text = "Paylaş")
                        }
                    }
                }
            }
        }

        if (posts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isMember) "Henüz paylaşım yok" else "Bu grubun paylaşımlarını görmek için üye olmalısın",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(posts) { post ->
                    ListItem(
                        post = post,
                        currentUserId = currentUserId ?: "",
                        onLike = {},
                        onComment = {},
                        onSave = {},
                        onRepost = {},
                        onProfileClick = {},
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MembersSection(members: List<User>) {
    if (members.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Henüz üye yok",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(members) { member ->
                MemberRow(user = member)
            }
        }
    }
}

@Composable
private fun MemberRow(user: User) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = user.displayName.ifBlank { user.username.ifBlank { "Kullanıcı" } },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
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
private fun InviteFriendsDialog(
    friends: List<User>,
    selectedFriendIds: Set<String>,
    isSubmitting: Boolean,
    onToggle: (String) -> Unit,
    onDismiss: () -> Unit,
    onInvite: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Arkadaş Davet Et") },
        text = {
            if (friends.isEmpty()) {
                Text(
                    text = "Davet edebileceğin uygun arkadaş bulunmuyor.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    friends.forEach { friend ->
                        FriendInviteRow(
                            user = friend,
                            isSelected = selectedFriendIds.contains(friend.id),
                            onToggle = { onToggle(friend.id) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onInvite,
                enabled = selectedFriendIds.isNotEmpty() && !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "Davet Gönder")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "İptal")
            }
        }
    )
}

@Composable
private fun FriendInviteRow(
    user: User,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.material3.Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggle() }
        )
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(
                text = user.displayName.ifBlank { user.username.ifBlank { "Kullanıcı" } },
                style = MaterialTheme.typography.bodyMedium
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
