package com.das3kn.iz.ui.presentation.groups

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.das3kn.iz.ui.presentation.navigation.MainNavTarget
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupSettingsScreen(
    groupId: String,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val currentUser = remember { GroupMockData.currentUser }
    val groupDetailState by GroupMockData.groupDetailFlow(groupId)
        .collectAsStateWithLifecycle(initialValue = GroupMockData.groupDetail(groupId))

    val detail = groupDetailState

    if (detail == null) {
        LaunchedEffect(groupId) {
            navController.popBackStack()
        }
        Surface(modifier = modifier.fillMaxSize()) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(text = "Grup bulunamadı", style = MaterialTheme.typography.bodyLarge)
            }
        }
        return
    }

    val group = detail.group
    val members = remember(detail.members) { detail.members }
    val isAdmin = group.admin.id == currentUser.id

    var groupName by rememberSaveable(groupId) { mutableStateOf(group.name) }
    var groupDescription by rememberSaveable(groupId) { mutableStateOf(group.description) }
    var groupImageUrl by rememberSaveable(groupId) { mutableStateOf(group.imageUrl) }
    var isPrivate by rememberSaveable(groupId) { mutableStateOf(group.isPrivate) }
    var selectedAdminId by rememberSaveable(groupId) { mutableStateOf(group.admin.id) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(group.id) {
        groupName = group.name
        groupDescription = group.description
        groupImageUrl = group.imageUrl
        isPrivate = group.isPrivate
        selectedAdminId = group.admin.id
    }

    val hasChanges = isAdmin && (
        groupName != group.name ||
            groupDescription != group.description ||
            groupImageUrl != group.imageUrl ||
            isPrivate != group.isPrivate ||
            selectedAdminId != group.admin.id
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Grup Ayarları", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    if (isAdmin) {
                        Button(
                            enabled = hasChanges,
                            onClick = {
                                GroupMockData.updateGroup(groupId) { current ->
                                    val newAdmin = members.firstOrNull { it.id == selectedAdminId } ?: current.admin
                                    current.copy(
                                        name = groupName.trim().ifBlank { current.name },
                                        description = groupDescription.trim(),
                                        imageUrl = groupImageUrl.trim().ifBlank { current.imageUrl },
                                        isPrivate = isPrivate,
                                        admin = newAdmin
                                    )
                                }
                                navController.popBackStack()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7C3AED),
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFFD8B4FE),
                                disabledContentColor = Color.White
                            ),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.padding(end = 4.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text(text = "Kaydet")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        containerColor = Color(0xFFF3F4F6)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SettingsSection(title = "Grup Bilgileri") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box {
                            AsyncImage(
                                model = groupImageUrl,
                                contentDescription = group.name,
                                modifier = Modifier
                                    .size(128.dp)
                                    .clip(RoundedCornerShape(28.dp)),
                                contentScale = ContentScale.Crop
                            )
                            if (isAdmin) {
                                IconButton(
                                    onClick = { /* TODO: media picker */ },
                                    modifier = Modifier
                                        .align(Alignment.BottomEnd)
                                        .background(Color(0xFF7C3AED), shape = CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CameraAlt,
                                        contentDescription = "Görseli değiştir",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }

                    SettingsTextField(
                        label = "Grup Adı",
                        value = groupName,
                        onValueChange = { groupName = it },
                        enabled = isAdmin,
                        placeholder = "Grup adı"
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    SettingsTextField(
                        label = "Açıklama",
                        value = groupDescription,
                        onValueChange = { groupDescription = it },
                        enabled = isAdmin,
                        placeholder = "Grup açıklaması",
                        singleLine = false,
                        minLines = 3
                    )
                    if (isAdmin) {
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingsTextField(
                            label = "Grup Görseli URL",
                            value = groupImageUrl,
                            onValueChange = { groupImageUrl = it },
                            enabled = true,
                            placeholder = "https://..."
                        )
                    }
                }
            }

            item { Divider() }

            item {
                SettingsSection(title = "Gizlilik") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val privacyIcon = if (isPrivate) Icons.Filled.Lock else Icons.Filled.Public
                            val privacyLabel = if (isPrivate) "Gizli Grup" else "Herkese Açık"
                            val privacyDescription = if (isPrivate) {
                                "Sadece üyeler içeriği görebilir"
                            } else {
                                "Herkes içeriği görebilir"
                            }
                            Icon(
                                imageVector = privacyIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(text = privacyLabel, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = privacyDescription,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isPrivate,
                            onCheckedChange = { isPrivate = it },
                            enabled = isAdmin,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF7C3AED)
                            )
                        )
                    }
                }
            }

            if (isAdmin && members.isNotEmpty()) {
                item { Divider() }

                item {
                    SettingsSection(title = "Yönetici Seç") {
                        Text(
                            text = "Başka bir üyeyi yönetici olarak atayabilirsiniz",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            members.forEach { member ->
                                AdminCandidateRow(
                                    member = member,
                                    isSelected = member.id == selectedAdminId,
                                    onSelect = { selectedAdminId = member.id }
                                )
                            }
                        }
                    }
                }
            }

            if (isAdmin) {
                item { Divider() }

                item {
                    SettingsSection(title = "Tehlikeli Bölge", titleColor = Color(0xFFEF4444)) {
                        Text(
                            text = "Bu işlem geri alınamaz. Tüm paylaşımlar ve üyeler silinecektir.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFEF4444)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFEF4444))
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                tint = Color(0xFFEF4444)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Grubu Sil", color = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        GroupMockData.deleteGroup(groupId)
                        navController.popBackStack(MainNavTarget.GroupsScreen.route, false)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444),
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Grubu Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(text = "İptal")
                }
            },
            title = { Text(text = "Grubu silmek istediğinize emin misiniz?") },
            text = {
                Text(
                    text = "Bu işlem geri alınamaz. Grubun tüm içeriği kalıcı olarak silinecektir.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    modifier: Modifier = Modifier,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = titleColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun SettingsTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            singleLine = singleLine,
            minLines = minLines,
            placeholder = { Text(text = placeholder) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color(0xFF7C3AED),
                cursorColor = Color(0xFF7C3AED)
            )
        )
    }
}

@Composable
private fun AdminCandidateRow(
    member: GroupUserUiModel,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onSelect() }
            .background(if (isSelected) Color(0xFFF5F3FF) else Color.Transparent)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = member.avatarUrl,
                contentDescription = member.name,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = member.name, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "@${member.username}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (isSelected) {
            Text(
                text = "Yönetici",
                color = Color(0xFF7C3AED),
                fontWeight = FontWeight.Bold
            )
        } else {
            OutlinedButton(
                onClick = onSelect,
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(text = "Yönetici Yap")
            }
        }
    }
}
