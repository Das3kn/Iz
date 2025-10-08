package com.das3kn.iz.data.supabase

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.days
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SupabaseStorageService @Inject constructor(
    private val storage: Storage
) {
    
    suspend fun uploadMedia(
        context: Context,
        uri: Uri,
        postId: String,
        fileName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            android.util.Log.d("SupabaseStorageService", "Starting upload - postId: $postId, fileName: $fileName")
            
            val bucket = storage.from("media")
            val filePath = "posts/$postId/$fileName"
            android.util.Log.d("SupabaseStorageService", "File path: $filePath")
            
            // Get input stream from URI
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Could not open file"))
            
            // Convert InputStream to ByteArray
            val bytes = inputStream.readBytes()
            inputStream.close()
            android.util.Log.d("SupabaseStorageService", "File size: ${bytes.size} bytes")
            
            // Upload to Supabase Storage
            android.util.Log.d("SupabaseStorageService", "Starting upload to Supabase...")
            bucket.upload(filePath, bytes)
            android.util.Log.d("SupabaseStorageService", "Upload completed successfully")
            
            // Get public URL - try both methods
            val publicUrl = try {
                // First try to get public URL directly
                bucket.publicUrl(filePath)
            } catch (e: Exception) {
                // If that fails, use signed URL
                bucket.createSignedUrl(filePath, expiresIn = 365.days)
            }
            android.util.Log.d("SupabaseStorageService", "Upload successful: $publicUrl")
            Result.success(publicUrl)
        } catch (e: Exception) {
            android.util.Log.e("SupabaseStorageService", "Upload failed: ${e.message}", e)
            android.util.Log.e("SupabaseStorageService", "Exception type: ${e.javaClass.simpleName}")
            Result.failure(e)
        }
    }
    
    suspend fun uploadMultipleMedia(
        context: Context,
        uris: List<Uri>,
        postId: String
    ): Result<List<String>> = withContext(Dispatchers.IO) {
        try {
            val timestamp = System.currentTimeMillis()
            val uploadResults = uris.mapIndexed { index, uri ->
                val extension = context.getFileExtension(uri)
                val fileName = buildString {
                    append("media_${timestamp}_$index")
                    if (!extension.isNullOrBlank()) {
                        append('.')
                        append(extension)
                    }
                }
                val result = uploadMedia(context, uri, postId, fileName)
                result.getOrThrow()
            }
            Result.success(uploadResults)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun deleteMedia(postId: String, fileName: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val bucket = storage.from("media")
            val filePath = "posts/$postId/$fileName"
            bucket.delete(filePath)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

private fun Context.getFileExtension(uri: Uri): String? {
    val contentResolver = contentResolver
    val mimeType = contentResolver.getType(uri)
    if (!mimeType.isNullOrEmpty()) {
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        if (!extension.isNullOrEmpty()) {
            return extension
        }
    }

    val path = uri.path ?: return null
    val lastDotIndex = path.lastIndexOf('.')
    return if (lastDotIndex != -1 && lastDotIndex < path.length - 1) {
        path.substring(lastDotIndex + 1)
    } else {
        null
    }
}
