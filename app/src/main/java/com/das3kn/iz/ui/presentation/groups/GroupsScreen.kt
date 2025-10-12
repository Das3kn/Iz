package com.das3kn.iz.ui.presentation.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.das3kn.iz.R
import com.das3kn.iz.ui.presentation.navigation.MainNavTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupsScreen(
    modifier: Modifier = Modifier,
    navController: NavHostController
) {
    val currentUser = remember { GroupMockData.currentUser }

    var groups by remember { mutableStateOf(GroupMockData.initialGroups()) }
    var isCreateGroupOpen by remember { mutableStateOf(false) }
    var groupName by remember { mutableStateOf("") }
    var groupDescription by remember { mutableStateOf("") }
    var groupImageUrl by remember { mutableStateOf("") }
    var searchQuery by remember { mutableStateOf("") }

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
                            Icon(imageVector = Icons.Filled.Add, contentDescription = "Yeni grup oluştur")
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text(text = "Grup ara...") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = {
                            Icon(imageVector = Icons.Filled.Search, contentDescription = null)
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = Color(0xFF7C3AED),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { /* no-op */ })
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
                        onSelect = {
                            navController.navigate("${MainNavTarget.GroupContentScreen.route}/${group.id}/${group.isJoined}")
                        },
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
                groupName = ""
                groupDescription = ""
                groupImageUrl = ""
                isCreateGroupOpen = false
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
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
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

private const val DEFAULT_GROUP_IMAGE = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=800&h=600&fit=crop"
