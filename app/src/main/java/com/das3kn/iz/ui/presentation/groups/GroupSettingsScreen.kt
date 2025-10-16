package com.das3kn.iz.ui.presentation.groups

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
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
    var groupAvatarUrl by rememberSaveable(groupId) { mutableStateOf(group.avatarUrl) }
    var groupCoverUrl by rememberSaveable(groupId) { mutableStateOf(group.imageUrl) }
    var isPrivate by rememberSaveable(groupId) { mutableStateOf(group.isPrivate) }
    var selectedAdminId by rememberSaveable(groupId) { mutableStateOf(group.admin.id) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(group.id) {
        groupName = group.name
        groupDescription = group.description
        groupAvatarUrl = group.avatarUrl
        groupCoverUrl = group.imageUrl
        isPrivate = group.isPrivate
        selectedAdminId = group.admin.id
    }

    val hasChanges = isAdmin && (
        groupName != group.name ||
            groupDescription != group.description ||
            groupAvatarUrl != group.avatarUrl ||
            groupCoverUrl != group.imageUrl ||
            isPrivate != group.isPrivate ||
            selectedAdminId != group.admin.id
    )

    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { groupCoverUrl = it.toString() }
    }
    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { groupAvatarUrl = it.toString() }
    }

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
                                        imageUrl = groupCoverUrl.trim().ifBlank { current.imageUrl },
                                        avatarUrl = groupAvatarUrl.trim().ifBlank { current.avatarUrl },
                                        isPrivate = isPrivate,
                                        admin = newAdmin
                                    )
                                }
                                navController.popBackStack()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary,
                                disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                disabledContentColor = MaterialTheme.colorScheme.onPrimary
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
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsSection(title = "Grup Bilgileri") {
                    GroupMediaSection(
                        coverUrl = groupCoverUrl,
                        avatarUrl = groupAvatarUrl,
                        canEdit = isAdmin,
                        onChangeCover = { coverPickerLauncher.launch("image/*") },
                        onChangeAvatar = { avatarPickerLauncher.launch("image/*") }
                    )

                    if (isAdmin) {
                        Text(
                            text = "Galeri butonlarını kullanarak kapak ve profil görsellerini güncelleyebilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
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
                            label = "Kapak Görseli URL",
                            value = groupCoverUrl,
                            onValueChange = { groupCoverUrl = it },
                            enabled = true,
                            placeholder = "https://..."
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        SettingsTextField(
                            label = "Profil Fotoğrafı URL",
                            value = groupAvatarUrl,
                            onValueChange = { groupAvatarUrl = it },
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
                                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                checkedTrackColor = MaterialTheme.colorScheme.primary,
                                uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                                uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant,
                                disabledCheckedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
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
                    SettingsSection(title = "Tehlikeli Bölge", titleColor = MaterialTheme.colorScheme.error) {
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
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Grubu Sil", color = MaterialTheme.colorScheme.error)
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
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.08f))
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
private fun GroupMediaSection(
    coverUrl: String,
    avatarUrl: String,
    canEdit: Boolean,
    onChangeCover: () -> Unit,
    onChangeAvatar: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        GroupCoverPreview(
            coverUrl = coverUrl,
            canEdit = canEdit,
            onChangeCover = onChangeCover,
            modifier = Modifier.align(Alignment.TopCenter)
        )
        GroupAvatarPreview(
            avatarUrl = avatarUrl,
            canEdit = canEdit,
            onChangeAvatar = onChangeAvatar,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = (-36).dp)
        )
    }
    Spacer(modifier = Modifier.height(48.dp))
}

@Composable
private fun GroupCoverPreview(
    coverUrl: String,
    canEdit: Boolean,
    onChangeCover: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = coverUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.3f))
                        )
                    )
            )
            if (canEdit) {
                IconButton(
                    onClick = onChangeCover,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = "Kapak görselini değiştir")
                }
            }
        }
    }
}

@Composable
private fun GroupAvatarPreview(
    avatarUrl: String,
    canEdit: Boolean,
    onChangeAvatar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.size(132.dp)) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(32.dp),
            tonalElevation = 3.dp,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f))
        ) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        if (canEdit) {
            IconButton(
                onClick = onChangeAvatar,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = "Profil fotoğrafını değiştir")
            }
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
            placeholder = {
                Text(
                    text = placeholder,
                    color = SettingsPlaceholderGray,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            textStyle = MaterialTheme.typography.bodyMedium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SettingsPrimaryPurple,
                unfocusedBorderColor = SettingsDividerGray,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = SettingsPrimaryPurple,
                focusedPlaceholderColor = SettingsPlaceholderGray,
                unfocusedPlaceholderColor = SettingsPlaceholderGray,
                disabledBorderColor = SettingsDividerGray,
                disabledContainerColor = Color.White,
                disabledPlaceholderColor = SettingsPlaceholderGray.copy(alpha = 0.6f),
                disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                disabledLeadingIconColor = SettingsPlaceholderGray.copy(alpha = 0.6f),
                disabledTrailingIconColor = SettingsPlaceholderGray.copy(alpha = 0.6f),
                focusedLeadingIconColor = SettingsPrimaryPurple,
                unfocusedLeadingIconColor = SettingsTextLightGray,
                focusedTrailingIconColor = SettingsTextGray,
                unfocusedTrailingIconColor = SettingsTextLightGray
            )
        )
    }
}

private val SettingsPrimaryPurple = Color(0xFF9333EA)
private val SettingsDividerGray = Color(0xFFE5E7EB)
private val SettingsPlaceholderGray = Color(0xFF9CA3AF)
private val SettingsTextGray = Color(0xFF4B5563)
private val SettingsTextLightGray = Color(0xFF9CA3AF)

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
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent
            )
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
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        } else {
            OutlinedButton(
                onClick = onSelect,
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
            ) {
                Text(text = "Yönetici Yap")
            }
        }
    }
}
