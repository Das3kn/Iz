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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import com.das3kn.iz.ui.presentation.navigation.MainNavTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    navController: NavHostController,
    viewModel: GroupsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var isCreationDialogVisible by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var selectedFriendIds by remember { mutableStateOf(setOf<String>()) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Gruplarım", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { isCreationDialogVisible = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Yeni grup")
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                uiState.groups.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Henüz bir grubun yok",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Yeni bir grup oluşturarak arkadaşlarını davet edebilirsin.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.groups) { group ->
                            GroupCard(
                                group = group,
                                onClick = {
                                    navController.navigate("${MainNavTarget.GroupContentScreen.route}/${group.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (isCreationDialogVisible) {
        GroupCreationDialog(
            name = groupName,
            description = groupDescription,
            friends = uiState.friends,
            selectedFriendIds = selectedFriendIds,
            onNameChange = { groupName = it },
            onDescriptionChange = { groupDescription = it },
            onToggleFriend = { friendId ->
                selectedFriendIds = if (selectedFriendIds.contains(friendId)) {
                    selectedFriendIds - friendId
                } else {
                    selectedFriendIds + friendId
                }
            },
            onDismiss = {
                isCreationDialogVisible = false
                groupName = ""
                groupDescription = ""
                selectedFriendIds = emptySet()
            },
            onCreate = {
                viewModel.createGroup(groupName, groupDescription, selectedFriendIds.toList())
                isCreationDialogVisible = false
                groupName = ""
                groupDescription = ""
                selectedFriendIds = emptySet()
            },
            isSubmitting = uiState.isCreatingGroup
        )
    }
}

@Composable
private fun GroupCard(
    group: Group,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = group.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (group.description.isNotBlank()) {
                Text(
                    text = group.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${group.memberIds.size} üye",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (group.pendingInvites.isNotEmpty()) {
                    Text(
                        text = "${group.pendingInvites.size} bekleyen davet",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupCreationDialog(
    name: String,
    description: String,
    friends: List<User>,
    selectedFriendIds: Set<String>,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onToggleFriend: (String) -> Unit,
    onDismiss: () -> Unit,
    onCreate: () -> Unit,
    isSubmitting: Boolean
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Yeni Grup Oluştur") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text(text = "Grup Adı") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = { Text(text = "Grup Açıklaması") },
                    minLines = 2
                )
                if (friends.isNotEmpty()) {
                    Text(
                        text = "Arkadaşlarını davet et",
                        style = MaterialTheme.typography.labelLarge
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp)
                    ) {
                        items(friends) { friend ->
                            FriendSelectableRow(
                                user = friend,
                                isSelected = selectedFriendIds.contains(friend.id),
                                onToggle = { onToggleFriend(friend.id) }
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Arkadaş eklemek için önce arkadaşlık kurmalısın.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onCreate,
                enabled = name.isNotBlank() && !isSubmitting
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(text = "Oluştur")
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
private fun FriendSelectableRow(
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
        Checkbox(
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
