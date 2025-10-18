package com.das3kn.iz.ui.presentation.posts

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.das3kn.iz.ui.presentation.auth.AuthViewModel
import com.das3kn.iz.ui.presentation.posts.MediaSelectionTab.IMAGE
import com.das3kn.iz.ui.presentation.posts.MediaSelectionTab.VIDEO
import com.das3kn.iz.utils.MediaPicker
import com.das3kn.iz.utils.rememberMediaPickerLauncher

private enum class MediaSelectionTab { IMAGE, VIDEO }
private enum class MediaSourceOption { GALLERY, CAMERA }

@Composable
fun CreatePostScreen(
    onNavigateBack: () -> Unit,
    onPostCreated: () -> Unit = {},
    groupId: String? = null,
    viewModel: CreatePostViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val userProfileState by authViewModel.userProfile.collectAsState()
    val isLoadingProfile = userProfileState == null

    val scrollState = rememberScrollState()
    val context = LocalContext.current

    val characterLimit = 280
    val hasContent = uiState.content.trim().isNotEmpty()
    val hasMedia = uiState.selectedMediaUris.isNotEmpty()
    val withinCharacterLimit = uiState.content.length <= characterLimit

    var showMediaInput by rememberSaveable { mutableStateOf(false) }
    var selectedMediaTab by rememberSaveable { mutableStateOf(IMAGE) }
    val canAddMoreMedia = uiState.selectedMediaUris.size < 4
    val canPost = (hasContent || hasMedia) && withinCharacterLimit && !uiState.isLoading

    var pendingImageUri by remember { mutableStateOf<Uri?>(null) }
    var pendingVideoUri by remember { mutableStateOf<Uri?>(null) }

    val mediaPicker = rememberMediaPickerLauncher(
        onImageSelected = { uri ->
            viewModel.addMediaUri(uri, false)
            selectedMediaTab = IMAGE
            showMediaInput = false
        },
        onVideoSelected = { uri ->
            viewModel.addMediaUri(uri, true)
            selectedMediaTab = VIDEO
            showMediaInput = false
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingImageUri?.let { uri ->
                viewModel.addMediaUri(uri, false)
                selectedMediaTab = IMAGE
                showMediaInput = false
            }
        }
        pendingImageUri = null
    }

    val videoCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        if (success) {
            pendingVideoUri?.let { uri ->
                viewModel.addMediaUri(uri, true)
                selectedMediaTab = VIDEO
                showMediaInput = false
            }
        }
        pendingVideoUri = null
    }

    LaunchedEffect(canAddMoreMedia) {
        if (!canAddMoreMedia) {
            showMediaInput = false
        }
    }

    LaunchedEffect(uiState.isPostCreated) {
        if (uiState.isPostCreated) {
            onPostCreated()
            onNavigateBack()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        containerColor = Color.White,
        topBar = {
            CreatePostTopBar(
                title = if (groupId.isNullOrBlank()) "Yeni Paylaşım" else "Gruba Paylaş",
                isLoading = uiState.isLoading,
                canPost = canPost,
                onNavigateBack = onNavigateBack,
                onShare = { viewModel.createPost(context) }
            )
        },
        bottomBar = {
            CreatePostBottomBar(
                modifier = Modifier.navigationBarsPadding(),
                imageCount = uiState.selectedMediaUris.count { !it.isVideo },
                videoCount = uiState.selectedMediaUris.count { it.isVideo },
                showMediaInput = showMediaInput,
                canAddMoreMedia = canAddMoreMedia,
                onRequestImage = {
                    showMediaInput = true
                    selectedMediaTab = IMAGE
                },
                onRequestVideo = {
                    showMediaInput = true
                    selectedMediaTab = VIDEO
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .imePadding()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3F4F6)),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isLoadingProfile -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            }
                            userProfileState?.profileImageUrl?.isNotBlank() == true -> {
                                AsyncImage(
                                    model = userProfileState!!.profileImageUrl,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                Text(
                                    text = userProfileState?.displayName?.firstOrNull()?.uppercase()
                                        ?: userProfileState?.username?.firstOrNull()?.uppercase()
                                        ?: "U",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        if (isLoadingProfile) {
                            Surface(
                                modifier = Modifier
                                    .height(16.dp)
                                    .width(120.dp),
                                color = Color(0xFFE5E7EB),
                                shape = RoundedCornerShape(8.dp)
                            ) {}
                        } else {
                            Text(
                                text = userProfileState?.displayName
                                    ?: userProfileState?.username
                                    ?: "Kullanıcı",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = "Herkes görebilir",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                TextField(
                    value = uiState.content,
                    onValueChange = viewModel::updateContent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    placeholder = { Text("Ne düşünüyorsun?") },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF8B5CF6)
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    val charColor = if (withinCharacterLimit) Color(0xFF9CA3AF) else Color(0xFFEF4444)
                    Text(
                        text = "${uiState.content.length} / $characterLimit",
                        style = MaterialTheme.typography.bodySmall,
                        color = charColor
                    )
                }

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFFFF1F2),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = uiState.error ?: "",
                            modifier = Modifier.padding(16.dp),
                            color = Color(0xFFB91C1C),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                if (uiState.selectedMediaUris.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Medya (${uiState.selectedMediaUris.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val rows = uiState.selectedMediaUris.chunked(2)
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        rows.forEachIndexed { rowIndex, rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowItems.forEachIndexed { columnIndex, mediaItem ->
                                    val absoluteIndex = rowIndex * 2 + columnIndex
                                    MediaPreviewItem(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f),
                                        item = mediaItem,
                                        onRemove = { viewModel.removeMediaUri(absoluteIndex) }
                                    )
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
                    MediaInputCard(
                        selectedTab = selectedMediaTab,
                        onClose = {
                            showMediaInput = false
                        },
                        onTabSelected = { selectedMediaTab = it },
                        onConfirmSelection = { tab, source ->
                            if (!canAddMoreMedia) return@MediaInputCard

                            when (source) {
                                MediaSourceOption.GALLERY -> {
                                    if (MediaPicker.hasStoragePermission(context)) {
                                        if (tab == IMAGE) {
                                            mediaPicker.pickImage()
                                        } else {
                                            mediaPicker.pickVideo()
                                        }
                                    } else {
                                        mediaPicker.requestPermissions()
                                    }
                                }

                                MediaSourceOption.CAMERA -> {
                                    if (MediaPicker.hasCameraPermission(context)) {
                                        if (tab == IMAGE) {
                                            val imageFile = MediaPicker.createImageFile(context)
                                            val uri = MediaPicker.getImageUri(context, imageFile)
                                            pendingImageUri = uri
                                            cameraLauncher.launch(uri)
                                        } else {
                                            val videoFile = MediaPicker.createVideoFile(context)
                                            val uri = MediaPicker.getVideoUri(context, videoFile)
                                            pendingVideoUri = uri
                                            videoCameraLauncher.launch(uri)
                                        }
                                    } else {
                                        mediaPicker.requestPermissions()
                                    }
                                }
                            }
                        },
                        canAddMoreMedia = canAddMoreMedia
                    )
                }

                Spacer(modifier = Modifier.height(120.dp))
            }
        }
    }
}

@Composable
private fun CreatePostTopBar(
    title: String,
    isLoading: Boolean,
    canPost: Boolean,
    onNavigateBack: () -> Unit,
    onShare: () -> Unit
) {
    Surface(color = Color.White) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Geri"
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onShare,
                    enabled = canPost,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8B5CF6),
                        disabledContainerColor = Color(0xFF8B5CF6).copy(alpha = 0.5f),
                        contentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(text = "Paylaş")
                    }
                }
            }
            Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
        }
    }
}

@Composable
private fun MediaPreviewItem(
    modifier: Modifier,
    item: MediaItem,
    onRemove: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (item.isVideo) Color(0xFF111827) else Color(0xFFF3F4F6)),
        contentAlignment = Alignment.Center
    ) {
        if (item.isVideo) {
            AsyncImage(
                model = item.uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.45f
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        } else {
            AsyncImage(
                model = item.uri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        IconButton(
            onClick = onRemove,
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
                text = if (item.isVideo) "Video" else "Fotoğraf",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White
            )
        }
    }
}

@Composable
private fun MediaInputCard(
    selectedTab: MediaSelectionTab,
    onClose: () -> Unit,
    onTabSelected: (MediaSelectionTab) -> Unit,
    onConfirmSelection: (MediaSelectionTab, MediaSourceOption) -> Unit,
    canAddMoreMedia: Boolean
) {
    var selectedSource by rememberSaveable { mutableStateOf(MediaSourceOption.GALLERY) }

    LaunchedEffect(selectedTab) {
        selectedSource = MediaSourceOption.GALLERY
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF9FAFB)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
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
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "Kapat")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val tabs = listOf(IMAGE to "Fotoğraf", VIDEO to "Video")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                tabs.forEach { (tab, label) ->
                    val isSelected = tab == selectedTab
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFF8B5CF6) else Color.White,
                        border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE5E7EB))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onTabSelected(tab) }
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val icon = if (tab == IMAGE) Icons.Filled.Image else Icons.Filled.PlayArrow
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Color(0xFF6B7280)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else Color(0xFF374151),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                val galleryTitle = if (selectedTab == IMAGE) "Galeriden Fotoğraf Seç" else "Galeriden Video Seç"
                val cameraTitle = if (selectedTab == IMAGE) "Kamera ile Fotoğraf Çek" else "Kamera ile Video Çek"
                val gallerySubtitle = "Telefon galerisinden paylaşımına ekle"
                val cameraSubtitle = "Anında çekim yap"

                MediaSourceSelectionOption(
                    icon = if (selectedTab == IMAGE) Icons.Filled.Image else Icons.Filled.PlayArrow,
                    title = galleryTitle,
                    subtitle = gallerySubtitle,
                    isSelected = selectedSource == MediaSourceOption.GALLERY,
                    enabled = canAddMoreMedia,
                    onClick = { selectedSource = MediaSourceOption.GALLERY }
                )

                MediaSourceSelectionOption(
                    icon = if (selectedTab == IMAGE) Icons.Filled.PhotoCamera else Icons.Filled.Videocam,
                    title = cameraTitle,
                    subtitle = cameraSubtitle,
                    isSelected = selectedSource == MediaSourceOption.CAMERA,
                    enabled = canAddMoreMedia,
                    onClick = { selectedSource = MediaSourceOption.CAMERA }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (selectedTab == VIDEO) {
                    "Galeriden video seçebilir veya kamera ile yeni bir video çekebilirsin."
                } else {
                    "Galeriden fotoğraf seçebilir veya kamera ile yeni bir fotoğraf çekebilirsin."
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { onConfirmSelection(selectedTab, selectedSource) },
                enabled = canAddMoreMedia,
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8B5CF6),
                    disabledContainerColor = Color(0xFF8B5CF6).copy(alpha = 0.4f)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Ekle")
            }
        }
    }
}

@Composable
private fun CreatePostBottomBar(
    modifier: Modifier = Modifier,
    imageCount: Int,
    videoCount: Int,
    showMediaInput: Boolean,
    canAddMoreMedia: Boolean,
    onRequestImage: () -> Unit,
    onRequestVideo: () -> Unit
) {
    Surface(color = Color.White, modifier = modifier) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onRequestImage,
                            enabled = !showMediaInput && canAddMoreMedia,
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = Color(0xFF8B5CF6),
                                disabledContentColor = Color(0xFF8B5CF6).copy(alpha = 0.4f)
                            )
                        ) {
                            Icon(imageVector = Icons.Filled.Image, contentDescription = "Fotoğraf Ekle")
                        }

                        IconButton(
                            onClick = onRequestVideo,
                            enabled = !showMediaInput && canAddMoreMedia,
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = Color(0xFF06B6D4),
                                disabledContentColor = Color(0xFF06B6D4).copy(alpha = 0.4f)
                            )
                        ) {
                            Icon(imageVector = Icons.Filled.PlayArrow, contentDescription = "Video Ekle")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(Color(0xFFE5E7EB))
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {},
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = Color(0xFF6B7280)
                            )
                        ) {
                            Icon(imageVector = Icons.Filled.LocationOn, contentDescription = "Konum")
                        }

                        IconButton(
                            onClick = {},
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = Color(0xFF6B7280)
                            )
                        ) {
                            Icon(imageVector = Icons.Filled.Mood, contentDescription = "Duygu")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (imageCount > 0) {
                        CountPill(
                            text = "$imageCount fotoğraf",
                            backgroundColor = Color(0xFFEDE9FE),
                            contentColor = Color(0xFF6D28D9)
                        )
                    }
                    if (videoCount > 0) {
                        CountPill(
                            text = "$videoCount video",
                            backgroundColor = Color(0xFFDCF4FF),
                            contentColor = Color(0xFF0E7490)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFF9FAFB),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "💡 İpucu: En fazla 4 medya ekleyebilirsiniz. Fotoğraf ve videoları karıştırabilirsiniz.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MediaSourceSelectionOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFFEDE9FE) else Color.White
    val borderColor = if (isSelected) Color(0xFF8B5CF6) else Color(0xFFE5E7EB)
    val iconTint = if (isSelected) Color(0xFF7C3AED) else Color(0xFF6B7280)
    val titleColor = if (isSelected) Color(0xFF4C1D95) else Color(0xFF1F2937)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (enabled) 1f else 0.5f),
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = enabled) { onClick() }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color(0xFF8B5CF6).copy(alpha = 0.15f) else Color(0xFFF3F4F6)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color(0xFF8B5CF6)
                )
            }
        }
    }
}

@Composable
private fun CountPill(
    text: String,
    backgroundColor: Color,
    contentColor: Color
) {
    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(50)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = contentColor,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold
        )
    }
}
