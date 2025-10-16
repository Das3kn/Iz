package com.das3kn.iz.ui.presentation.posts

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.das3kn.iz.R
import com.das3kn.iz.ui.presentation.auth.AuthViewModel
import com.das3kn.iz.utils.MediaPicker
import com.das3kn.iz.utils.rememberMediaPickerLauncher
import java.io.File

private enum class MediaSelectionTab { IMAGE, VIDEO }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    onPostCreated: () -> Unit = {},
    groupId: String? = null,
    viewModel: CreatePostViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val userProfile by authViewModel.userProfile.collectAsState()
    val isLoadingProfile = userProfile == null

    val characterLimit = 280

    var showMediaInput by rememberSaveable { mutableStateOf(false) }
    var selectedMediaTab by rememberSaveable { mutableStateOf(MediaSelectionTab.IMAGE) }
    var mediaUrl by rememberSaveable { mutableStateOf("") }

    val mediaPicker = rememberMediaPickerLauncher(
        onImageSelected = { uri ->
            viewModel.addMediaUri(uri, false)
        },
        onVideoSelected = { uri ->
            viewModel.addMediaUri(uri, true)
        },
        onPermissionDenied = { }
    )

    var currentCameraFile by remember { mutableStateOf<File?>(null) }
    var currentVideoFile by remember { mutableStateOf<File?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentCameraFile != null) {
            val uri = MediaPicker.getImageUri(context, currentCameraFile!!)
            viewModel.addMediaUri(uri, false)
        }
    }

    val videoCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success && currentVideoFile != null) {
            val uri = MediaPicker.getVideoUri(context, currentVideoFile!!)
            viewModel.addMediaUri(uri, true)
        }
    }

    LaunchedEffect(uiState.isPostCreated) {
        if (uiState.isPostCreated) {
            onPostCreated()
            onNavigateBack()
        }
    }

    val canPost = (uiState.content.isNotBlank() ||
        uiState.selectedMediaUris.isNotEmpty() ||
        uiState.selectedImages.isNotEmpty()) && !uiState.isLoading
    val canAddMoreMedia = uiState.selectedMediaUris.size < 4

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Surface(
                tonalElevation = 1.dp,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Geri")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (groupId.isNullOrBlank()) "Yeni Paylaşım" else "Gruba Paylaş",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Button(
                        onClick = { viewModel.createPost(context) },
                        enabled = canPost,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        ),
                        contentPadding = ButtonDefaults.ContentPadding
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Paylaş",
                                color = MaterialTheme.colorScheme.onPrimary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    showMediaInput = true
                                    selectedMediaTab = MediaSelectionTab.IMAGE
                                },
                                enabled = !showMediaInput && canAddMoreMedia,
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Filled.Image, contentDescription = "Fotoğraf Ekle")
                            }
                            IconButton(
                                onClick = {
                                    showMediaInput = true
                                    selectedMediaTab = MediaSelectionTab.VIDEO
                                },
                                enabled = !showMediaInput && canAddMoreMedia,
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = "Video Ekle")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Divider(
                                modifier = Modifier
                                    .height(24.dp)
                                    .width(1.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { }, colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )) {
                                Icon(Icons.Filled.LocationOn, contentDescription = "Konum")
                            }
                            IconButton(onClick = { }, colors = IconButtonDefaults.iconButtonColors(
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )) {
                                Icon(Icons.Filled.Mood, contentDescription = "Duygu")
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            val imageCount = uiState.selectedMediaUris.count { !it.isVideo }
                            val videoCount = uiState.selectedMediaUris.count { it.isVideo }
                            if (imageCount > 0) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("$imageCount fotoğraf") },
                                    enabled = false
                                )
                            }
                            if (videoCount > 0) {
                                AssistChip(
                                    onClick = {},
                                    label = { Text("$videoCount video") },
                                    enabled = false
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "İpucu: En fazla 4 medya ekleyebilirsiniz. Fotoğraf ve videoları karıştırabilirsiniz.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingProfile) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        val avatarUrl = userProfile?.profileImageUrl?.takeIf { it.isNotBlank() }
                        if (avatarUrl != null) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = userProfile?.displayName?.firstOrNull()?.uppercase()
                                    ?: userProfile?.username?.firstOrNull()?.uppercase()
                                    ?: "U",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                    if (isLoadingProfile) {
                        Surface(
                            tonalElevation = 1.dp,
                            modifier = Modifier
                                .height(16.dp)
                                .width(120.dp)
                        ) {}
                    } else {
                        Text(
                            text = userProfile?.displayName
                                ?: userProfile?.username
                                ?: "Kullanıcı",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = "Herkes görebilir",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = uiState.content,
                onValueChange = { viewModel.updateContent(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                placeholder = { Text("Ne düşünüyorsun?") },
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.primary
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                val charCountColor = if (uiState.content.length > characterLimit) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text(
                    text = "${uiState.content.length} / $characterLimit",
                    style = MaterialTheme.typography.bodySmall,
                    color = charCountColor
                )
            }

            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(12.dp))
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.error ?: "",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (uiState.selectedMediaUris.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Medya (${uiState.selectedMediaUris.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                val mediaRows = uiState.selectedMediaUris.chunked(2)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    mediaRows.forEachIndexed { rowIndex, rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEachIndexed { columnIndex, mediaItem ->
                                val absoluteIndex = rowIndex * 2 + columnIndex
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (mediaItem.isVideo) Color.Black else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (mediaItem.isVideo) {
                                        AsyncImage(
                                            model = mediaItem.uri,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                            alpha = 0.6f
                                        )
                                        Icon(
                                            imageVector = Icons.Filled.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(40.dp)
                                        )
                                    } else {
                                        AsyncImage(
                                            model = mediaItem.uri,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.removeMediaUri(absoluteIndex) },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .size(28.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Kaldır",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(8.dp),
                                        shape = RoundedCornerShape(50),
                                        color = Color.Black.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = if (mediaItem.isVideo) "Video" else "Fotoğraf",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            if (uiState.selectedImages.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Seçilen Görseller",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))
                val legacyRows = uiState.selectedImages.chunked(2)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    legacyRows.forEachIndexed { rowIndex, rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowItems.forEachIndexed { columnIndex, imageRes ->
                                val absoluteIndex = rowIndex * 2 + columnIndex
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                ) {
                                    Image(
                                        painter = painterResource(id = imageRes),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )

                                    IconButton(
                                        onClick = {
                                            val index = uiState.selectedImages.indexOf(imageRes)
                                            if (index != -1) viewModel.removeImage(index)
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp)
                                            .size(28.dp)
                                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Kaldır",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            if (showMediaInput) {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Medya Ekle",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            TextButton(
                                onClick = {
                                    showMediaInput = false
                                    mediaUrl = ""
                                    selectedMediaTab = MediaSelectionTab.IMAGE
                                }
                            ) {
                                Text("Kapat")
                            }
                        }

                        val tabs = listOf(MediaSelectionTab.IMAGE, MediaSelectionTab.VIDEO)
                        TabRow(
                            selectedTabIndex = tabs.indexOf(selectedMediaTab),
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.primary
                        ) {
                            tabs.forEach { tab ->
                                Tab(
                                    selected = tab == selectedMediaTab,
                                    onClick = { selectedMediaTab = tab },
                                    text = {
                                        Text(
                                            text = if (tab == MediaSelectionTab.IMAGE) "Fotoğraf" else "Video",
                                            maxLines = 1
                                        )
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (tab == MediaSelectionTab.IMAGE) Icons.Filled.Image else Icons.Filled.PlayArrow,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        TextField(
                            value = mediaUrl,
                            onValueChange = { mediaUrl = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = if (selectedMediaTab == MediaSelectionTab.IMAGE) "Fotoğraf URL'si girin" else "Video URL'si girin"
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )

                        if (mediaUrl.isNotBlank() && selectedMediaTab == MediaSelectionTab.IMAGE) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                shape = RoundedCornerShape(16.dp),
                                tonalElevation = 2.dp
                            ) {
                                AsyncImage(
                                    model = mediaUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxWidth(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        if (selectedMediaTab == MediaSelectionTab.VIDEO) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "YouTube, Vimeo veya direkt video URL'si ekleyebilirsiniz",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                val parsedUri: Uri = mediaUrl.trim().toUri()
                                viewModel.addMediaUri(parsedUri, selectedMediaTab == MediaSelectionTab.VIDEO)
                                mediaUrl = ""
                                showMediaInput = false
                                selectedMediaTab = MediaSelectionTab.IMAGE
                            },
                            enabled = mediaUrl.isNotBlank() && canAddMoreMedia,
                            modifier = Modifier.fillMaxWidth(),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Ekle")
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Alternatif seçenekler",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (MediaPicker.hasStoragePermission(context)) {
                                            mediaPicker.pickImage()
                                        } else {
                                            mediaPicker.requestPermissions()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    enabled = canAddMoreMedia
                                ) {
                                    Icon(Icons.Filled.Image, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Galeriden Fotoğraf")
                                }
                                Button(
                                    onClick = {
                                        if (MediaPicker.hasCameraPermission(context)) {
                                            val file = MediaPicker.createImageFile(context)
                                            currentCameraFile = file
                                            val uri = MediaPicker.getImageUri(context, file)
                                            cameraLauncher.launch(uri)
                                        } else {
                                            mediaPicker.requestPermissions()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    ),
                                    enabled = canAddMoreMedia
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.camera),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Kamera")
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (MediaPicker.hasStoragePermission(context)) {
                                            mediaPicker.pickVideo()
                                        } else {
                                            mediaPicker.requestPermissions()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    ),
                                    enabled = canAddMoreMedia
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Video")
                                }
                                Button(
                                    onClick = {
                                        if (MediaPicker.hasCameraPermission(context)) {
                                            val file = MediaPicker.createVideoFile(context)
                                            currentVideoFile = file
                                            val uri = MediaPicker.getVideoUri(context, file)
                                            videoCameraLauncher.launch(uri)
                                        } else {
                                            mediaPicker.requestPermissions()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    ),
                                    enabled = canAddMoreMedia
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.video),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Video Çek")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Gönderi Ayarları",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Herkes görebilir", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Konum ekle", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
