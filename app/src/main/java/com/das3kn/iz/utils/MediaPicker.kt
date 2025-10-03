package com.das3kn.iz.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object MediaPicker {
    
    fun createImageFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir("Pictures")
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }
    
    fun createVideoFile(context: Context): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = context.getExternalFilesDir("Movies")
        return File.createTempFile("VIDEO_${timeStamp}_", ".mp4", storageDir)
    }
    
    fun getImageUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
    
    fun getVideoUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
    
    fun hasStoragePermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
}

@Composable
fun rememberMediaPickerLauncher(
    onImageSelected: (Uri) -> Unit,
    onVideoSelected: (Uri) -> Unit,
    onPermissionDenied: () -> Unit = {}
): MediaPickerLauncher {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }
    
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onVideoSelected(it) }
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        // Success is handled by the calling code
    }
    
    val videoCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CaptureVideo()
    ) { success ->
        // Success is handled by the calling code
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (!allGranted) {
            onPermissionDenied()
        }
    }
    
    return remember {
        MediaPickerLauncher(
            imagePickerLauncher = imagePickerLauncher,
            videoPickerLauncher = videoPickerLauncher,
            cameraLauncher = cameraLauncher,
            videoCameraLauncher = videoCameraLauncher,
            permissionLauncher = permissionLauncher
        )
    }
}

data class MediaPickerLauncher(
    val imagePickerLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    val videoPickerLauncher: androidx.activity.result.ActivityResultLauncher<String>,
    val cameraLauncher: androidx.activity.result.ActivityResultLauncher<Uri>,
    val videoCameraLauncher: androidx.activity.result.ActivityResultLauncher<Uri>,
    val permissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>
) {
    fun pickImage() {
        imagePickerLauncher.launch("image/*")
    }
    
    fun pickVideo() {
        videoPickerLauncher.launch("video/*")
    }
    
    fun takePhoto(uri: Uri) {
        cameraLauncher.launch(uri)
    }
    
    fun takeVideo(uri: Uri) {
        videoCameraLauncher.launch(uri)
    }
    
    fun requestPermissions() {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            )
        )
    }
}
