package com.das3kn.iz.ui.presentation.groups

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage

@Composable
fun GroupSettingsScreen(
    groupId: String,
    navController: NavHostController,
    viewModel: GroupSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(groupId) {
        viewModel.load(groupId)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.navigateBackAfterSave, uiState.navigateUpToGroups) {
        when {
            uiState.navigateUpToGroups -> {
                navController.popBackStack()
                navController.popBackStack()
                viewModel.consumeNavigation()
            }
            uiState.navigateBackAfterSave -> {
                navController.popBackStack()
                viewModel.consumeNavigation()
            }
        }
    }

    val group = uiState.group

    var groupName by remember(group?.id) { mutableStateOf(group?.name.orEmpty()) }
    var groupDescription by remember(group?.id) { mutableStateOf(group?.description.orEmpty()) }
    var coverImageUrl by remember(group?.id) { mutableStateOf(group?.imageUrl.orEmpty()) }
    var profileImageUrl by remember(group?.id) { mutableStateOf(group?.profileImageUrl.orEmpty()) }
    var isPrivate by remember(group?.id) { mutableStateOf(group?.isPrivate ?: false) }

    var coverUri by remember(group?.id) { mutableStateOf<Uri?>(null) }
    var profileUri by remember(group?.id) { mutableStateOf<Uri?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showImagePickerDialog by remember { mutableStateOf(false) }

    val coverPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            coverUri = uri
            coverImageUrl = uri.toString()
        }
    }
    val profilePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            profileUri = uri
            profileImageUrl = uri.toString()
        }
    }

    LaunchedEffect(uiState.group) {
        uiState.group?.let { refreshed ->
            groupName = refreshed.name
            groupDescription = refreshed.description
            if (coverUri == null) {
                coverImageUrl = refreshed.imageUrl
            }
            if (profileUri == null) {
                profileImageUrl = refreshed.profileImageUrl
            }
            isPrivate = refreshed.isPrivate
        }
    }

    Scaffold(
        containerColor = Color(0xFFF9FAFB),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            SettingsTopBar(
                isAdmin = uiState.isAdmin,
                isSaving = uiState.isSaving,
                onBack = { navController.popBackStack() },
                onSave = {
                    val currentGroup = uiState.group ?: return@SettingsTopBar
                    viewModel.saveSettings(
                        currentGroup.id,
                        GroupSettingsData(
                            name = groupName.trim(),
                            description = groupDescription.trim(),
                            coverImageUrl = coverImageUrl.trim(),
                            profileImageUrl = profileImageUrl.trim().ifBlank { coverImageUrl.trim() },
                            isPrivate = isPrivate,
                        )
                    )
                }
            )
        }
    ) { innerPadding ->
        when {
            uiState.isLoading || group == null -> {
                LoadingState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
            else -> {
                GroupSettingsContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    isAdmin = uiState.isAdmin,
                    members = uiState.members,
                    isDeleting = uiState.isDeleting,
                    groupName = groupName,
                    onGroupNameChange = { groupName = it },
                    groupDescription = groupDescription,
                    onGroupDescriptionChange = { groupDescription = it },
                    coverImageUrl = coverImageUrl,
                    coverImageUri = coverUri,
                    onCoverImageUrlChange = {
                        coverUri = null
                        coverImageUrl = it
                    },
                    profileImageUrl = profileImageUrl,
                    profileImageUri = profileUri,
                    onProfileImageUrlChange = {
                        profileUri = null
                        profileImageUrl = it
                    },
                    isPrivate = isPrivate,
                    onPrivacyChange = { isPrivate = it },
                    onCoverClick = {
                        if (uiState.isAdmin) {
                            showImagePickerDialog = true
                        }
                    },
                    onDeleteClick = { showDeleteDialog = true }
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Grubu silmek istediğinize emin misiniz?") },
            text = {
                Text("Bu işlem geri alınamaz. Grubun tüm içeriği kalıcı olarak silinecektir.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        uiState.group?.let { viewModel.deleteGroup(it.id) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Grubu Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    if (showImagePickerDialog) {
        AlertDialog(
            onDismissRequest = { showImagePickerDialog = false },
            title = { Text("Görsel seç") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(
                        onClick = {
                            showImagePickerDialog = false
                            profilePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    ) { Text("Profil fotoğrafı seç") }
                    TextButton(
                        onClick = {
                            showImagePickerDialog = false
                            coverPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                    ) { Text("Arka plan görseli seç") }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImagePickerDialog = false }) {
                    Text("Kapat")
                }
            }
        )
    }
}

@Composable
private fun SettingsTopBar(
    isAdmin: Boolean,
    isSaving: Boolean,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    Surface(color = Color.White, shadowElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color(0xFF111827))
                ) {
                    Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Grup Ayarları",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF111827)
                )
            }
            if (isAdmin) {
                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7C3AED),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF7C3AED).copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(999.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(text = if (isSaving) "Kaydediliyor..." else "Kaydet")
                }
            }
        }
    }
}

@Composable
private fun GroupSettingsContent(
    modifier: Modifier = Modifier,
    isAdmin: Boolean,
    members: List<GroupUserUiModel>,
    isDeleting: Boolean,
    groupName: String,
    onGroupNameChange: (String) -> Unit,
    groupDescription: String,
    onGroupDescriptionChange: (String) -> Unit,
    coverImageUrl: String,
    coverImageUri: Uri?,
    onCoverImageUrlChange: (String) -> Unit,
    profileImageUrl: String,
    profileImageUri: Uri?,
    onProfileImageUrlChange: (String) -> Unit,
    isPrivate: Boolean,
    onPrivacyChange: (Boolean) -> Unit,
    onCoverClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    LazyColumn(
        modifier = modifier.background(Color(0xFFF3F4F6)),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(color = Color.White) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Grup Bilgileri",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF111827)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GroupImagePreview(
                        cover = coverImageUri ?: coverImageUrl,
                        profile = profileImageUri ?: profileImageUrl,
                        isAdmin = isAdmin,
                        onClick = onCoverClick
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        GroupTextField(
                            label = "Grup Adı",
                            value = groupName,
                            onValueChange = onGroupNameChange,
                            enabled = isAdmin,
                            placeholder = "Grup adı"
                        )
                        GroupTextField(
                            label = "Açıklama",
                            value = groupDescription,
                            onValueChange = onGroupDescriptionChange,
                            enabled = isAdmin,
                            placeholder = "Grup açıklaması",
                            singleLine = false,
                            minLines = 3
                        )
                        if (isAdmin) {
                            GroupTextField(
                                label = "Grup Kapak Görseli URL",
                                value = coverImageUrl,
                                onValueChange = onCoverImageUrlChange,
                                enabled = true,
                                placeholder = "https://..."
                            )
                            GroupTextField(
                                label = "Grup Profil Fotoğrafı URL",
                                value = profileImageUrl,
                                onValueChange = onProfileImageUrlChange,
                                enabled = true,
                                placeholder = "https://..."
                            )
                        }
                    }
                }
            }
        }

        item { Divider(color = Color(0xFFE5E7EB), thickness = 8.dp) }

        item {
            Surface(color = Color.White) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Gizlilik",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFF111827)
                    )
                    PrivacySection(
                        isPrivate = isPrivate,
                        onPrivacyChange = onPrivacyChange,
                        enabled = isAdmin
                    )
                }
            }
        }

        if (isAdmin) {
            item { Divider(color = Color(0xFFE5E7EB), thickness = 8.dp) }

            item {
                Surface(color = Color.White) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Yönetici Seç",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = Color(0xFF111827)
                            )
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = null,
                                tint = Color(0xFF6B7280),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Başka bir üyeyi yönetici olarak atayabilirsiniz",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF6B7280)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            members.forEach { member ->
                                MemberRow(member = member)
                            }
                        }
                    }
                }
            }

            item { Divider(color = Color(0xFFE5E7EB), thickness = 8.dp) }

            item {
                Surface(color = Color.White) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Tehlikeli Bölge",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFFEF4444)
                        )
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F2)),
                            border = BorderStroke(1.dp, Color(0xFFFECACA)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = null,
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(
                                            text = "Grubu Sil",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                            color = Color(0xFFEF4444)
                                        )
                                        Text(
                                            text = "Bu işlem geri alınamaz. Tüm paylaşımlar ve üyeler silinecektir.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF6B7280)
                                        )
                                    }
                                }
                                OutlinedButton(
                                    onClick = onDeleteClick,
                                    enabled = !isDeleting,
                                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(999.dp)
                                ) {
                                    Text(if (isDeleting) "Siliniyor..." else "Grubu Sil")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupImagePreview(
    cover: Any?,
    profile: Any?,
    isAdmin: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(128.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(Color(0xFFE5E7EB))
            .clickable(enabled = isAdmin, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        var hasCover = false
        when (cover) {
            is Uri -> {
                hasCover = true
                AsyncImage(model = cover, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            is String -> if (cover.isNotBlank()) {
                hasCover = true
                AsyncImage(model = cover, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
        }
        if (!hasCover) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = Color(0xFF9CA3AF),
                modifier = Modifier.size(48.dp)
            )
        }
        when (profile) {
            is Uri -> ProfileBadge(uri = profile, modifier = Modifier.align(Alignment.BottomStart))
            is String -> if (profile.isNotBlank()) {
                ProfileBadge(uri = profile, modifier = Modifier.align(Alignment.BottomStart))
            }
        }
        if (isAdmin) {
            Icon(
                imageVector = Icons.Filled.CameraAlt,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF7C3AED))
                    .padding(6.dp)
            )
        }
    }
}

@Composable
private fun ProfileBadge(uri: Any, modifier: Modifier = Modifier) {
    AsyncImage(
        model = uri,
        contentDescription = null,
        modifier = modifier
            .padding(8.dp)
            .size(40.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(2.dp)
            .clip(RoundedCornerShape(14.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun GroupTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF6B7280),
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            placeholder = { Text(placeholder) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF7C3AED),
                unfocusedBorderColor = Color(0xFFE5E7EB),
                disabledBorderColor = Color(0xFFE5E7EB),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                cursorColor = Color(0xFF7C3AED)
            )
        )
    }
}

@Composable
private fun PrivacySection(
    isPrivate: Boolean,
    onPrivacyChange: (Boolean) -> Unit,
    enabled: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isPrivate) Icons.Filled.Lock else Icons.Filled.Public,
                contentDescription = null,
                tint = Color(0xFF6B7280),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Gizli Grup",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF111827)
                )
                Text(
                    text = if (isPrivate) "Sadece üyeler içeriği görebilir" else "Herkes içeriği görebilir",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Switch(
            checked = isPrivate,
            onCheckedChange = onPrivacyChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFF7C3AED),
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFE5E7EB)
            )
        )
    }
}

@Composable
private fun MemberRow(member: GroupUserUiModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = member.avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF111827)
                )
                Text(
                    text = "@${member.username}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF6B7280)
                )
            }
        }
        OutlinedButton(
            onClick = { },
            shape = RoundedCornerShape(999.dp),
            border = BorderStroke(1.dp, Color(0xFFD1D5DB))
        ) {
            Text("Yönetici Yap")
        }
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
