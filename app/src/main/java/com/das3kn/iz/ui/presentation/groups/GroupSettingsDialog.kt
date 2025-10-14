package com.das3kn.iz.ui.presentation.groups

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.das3kn.iz.R

@Composable
fun GroupSettingsDialog(
    group: GroupUiModel,
    members: List<GroupUserUiModel>,
    isAdmin: Boolean,
    isSaving: Boolean,
    isDeleting: Boolean,
    onDismiss: () -> Unit,
    onSave: (GroupSettingsData) -> Unit,
    onDelete: () -> Unit
) {
    var groupName by remember(group.id) { mutableStateOf(group.name) }
    var description by remember(group.id) { mutableStateOf(group.description) }
    var imageUrl by remember(group.id) { mutableStateOf(group.imageUrl) }
    var isPrivate by remember(group.id) { mutableStateOf(group.isPrivate) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    LaunchedEffect(group) {
        groupName = group.name
        description = group.description
        imageUrl = group.imageUrl
        isPrivate = group.isPrivate
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF9FAFB),
            tonalElevation = 0.dp,
            shape = RoundedCornerShape(0.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SettingsTopBar(
                    onBack = onDismiss,
                    onSave = {
                        onSave(
                            GroupSettingsData(
                                name = groupName.trim(),
                                description = description.trim(),
                                imageUrl = imageUrl.trim(),
                                isPrivate = isPrivate
                            )
                        )
                    },
                    isAdmin = isAdmin,
                    isSaving = isSaving
                )
                Divider()
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        GroupInfoSection(
                            groupName = groupName,
                            onGroupNameChange = { groupName = it },
                            description = description,
                            onDescriptionChange = { description = it },
                            imageUrl = imageUrl,
                            onImageUrlChange = { imageUrl = it },
                            isAdmin = isAdmin,
                            imagePreview = imageUrl
                        )
                    }

                    item { Divider() }

                    item {
                        PrivacySection(
                            isPrivate = isPrivate,
                            onPrivacyChange = { isPrivate = it },
                            isAdmin = isAdmin
                        )
                    }

                    item { Divider() }

                    if (isAdmin) {
                        item {
                            AdminSelectionSection(members = members)
                        }

                        item { Divider() }

                          item {
                              DangerZoneSection(
                                  isDeleting = isDeleting,
                                  onDeleteClick = { showDeleteConfirmation = true }
                              )
                          }
                      }
                  }
              }
          }
      }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmation = false
                        onDelete()
                    },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text(text = if (isDeleting) "Siliniyor..." else "Grubu Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(text = "İptal")
                }
            },
            title = { Text(text = "Grubu silmek istediğinize emin misiniz?") },
            text = {
                Text(
                    text = "Bu işlem geri alınamaz. Grubun tüm içeriği kalıcı olarak silinecektir.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null,
                    tint = Color(0xFFEF4444)
                )
            },
            containerColor = AlertDialogDefaults.containerColor
        )
    }
}

@Composable
private fun SettingsTopBar(
    onBack: () -> Unit,
    onSave: () -> Unit,
    isAdmin: Boolean,
    isSaving: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Geri")
        }
        Text(
            text = "Grup Ayarları",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.weight(1f)
        )
        if (isAdmin) {
            Button(
                onClick = onSave,
                enabled = !isSaving,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(text = if (isSaving) "Kaydediliyor..." else "Kaydet")
            }
        }
    }
}

@Composable
private fun GroupInfoSection(
    groupName: String,
    onGroupNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    imageUrl: String,
    onImageUrlChange: (String) -> Unit,
    isAdmin: Boolean,
    imagePreview: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Grup Bilgileri",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(128.dp)
        ) {
            AsyncImage(
                model = imagePreview.ifBlank { imageUrl },
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.worker_image),
                error = painterResource(id = R.drawable.worker_image)
            )
            if (isAdmin) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF7C3AED))
                        .clickable(onClick = { /* TODO: Add image picker */ }),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = "Görsel değiştir",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Grup Adı", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
        OutlinedTextField(
            value = groupName,
            onValueChange = onGroupNameChange,
            enabled = isAdmin,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            placeholder = { Text(text = "Grup adı") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Açıklama", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            enabled = isAdmin,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            placeholder = { Text(text = "Grup açıklaması") },
            shape = RoundedCornerShape(12.dp),
            maxLines = 5
        )
        if (isAdmin) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Grup Görseli URL", style = MaterialTheme.typography.bodySmall, color = Color(0xFF6B7280))
            OutlinedTextField(
                value = imageUrl,
                onValueChange = onImageUrlChange,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text(text = "https://...") }
            )
        }
    }
}

@Composable
private fun PrivacySection(
    isPrivate: Boolean,
    onPrivacyChange: (Boolean) -> Unit,
    isAdmin: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Gizlilik",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = if (isPrivate) Icons.Filled.Lock else Icons.Filled.Public
                val description = if (isPrivate) {
                    "Sadece üyeler içeriği görebilir"
                } else {
                    "Herkes içeriği görebilir"
                }
                Surface(
                    modifier = Modifier.size(42.dp),
                    shape = CircleShape,
                    color = Color(0xFFF3F4F6)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF6B7280))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Gizli Grup", style = MaterialTheme.typography.bodyLarge)
                    Text(text = description, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B7280))
                }
            }
            Switch(
                checked = isPrivate,
                onCheckedChange = onPrivacyChange,
                enabled = isAdmin,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF7C3AED),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFE5E7EB)
                )
            )
        }
    }
}

@Composable
private fun AdminSelectionSection(members: List<GroupUserUiModel>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Yönetici Seç", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
            Icon(imageVector = Icons.Outlined.Settings, contentDescription = null, tint = Color(0xFF6B7280))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Başka bir üyeyi yönetici olarak atayabilirsiniz",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            members.forEach { member ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = member.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.worker_image),
                                error = painterResource(id = R.drawable.worker_image)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = member.name, style = MaterialTheme.typography.bodyLarge)
                                Text(text = "@${member.username}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF6B7280))
                            }
                        }
                        OutlinedButton(
                            onClick = { /* TODO: Change admin */ },
                            shape = RoundedCornerShape(999.dp),
                            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 6.dp)
                        ) {
                            Text(text = "Yönetici Yap")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DangerZoneSection(
    isDeleting: Boolean,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(
            text = "Tehlikeli Bölge",
            style = MaterialTheme.typography.titleMedium.copy(color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, Color(0xFFFECACA))
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = Color(0xFFFFE4E6)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Grubu Sil", style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFFEF4444)))
                    Text(
                        text = "Bu işlem geri alınamaz. Tüm paylaşımlar ve üyeler silinecektir.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onDeleteClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(999.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
            border = BorderStroke(1.dp, Color(0xFFEF4444)),
            contentPadding = PaddingValues(vertical = 12.dp),
            enabled = !isDeleting
        ) {
            Text(text = if (isDeleting) "Siliniyor..." else "Grubu Sil")
        }
    }
}
