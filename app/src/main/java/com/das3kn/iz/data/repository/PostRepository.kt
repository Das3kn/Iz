package com.das3kn.iz.data.repository

import com.das3kn.iz.data.model.Post
import com.das3kn.iz.data.model.Comment
import com.das3kn.iz.data.supabase.SupabaseStorageService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Context

@Singleton
class PostRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val supabaseStorageService: SupabaseStorageService
) {
    
    suspend fun createPost(post: Post): Result<Post> {
        return try {
            val docRef = firestore.collection("posts").add(post).await()
            // Post'a document ID'yi set et
            val updatedPost = post.copy(id = docRef.id)
            // Firestore'da post'u güncelle (ID ile birlikte)
            firestore.collection("posts").document(docRef.id).set(updatedPost).await()
            Result.success(updatedPost)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePost(post: Post): Result<Post> {
        return try {
            firestore.collection("posts").document(post.id).set(post).await()
            Result.success(post)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPosts(limit: Int = 20): Result<List<Post>> {
        return try {
            val snapshot = firestore.collection("posts")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            val posts = snapshot.documents.mapNotNull { it.toObject(Post::class.java) }
            android.util.Log.d("PostRepository", "Retrieved ${posts.size} posts")
            posts.forEach { post ->
                android.util.Log.d("PostRepository", "Post ${post.id} has ${post.mediaUrls.size} media URLs: ${post.mediaUrls}")
            }
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPostById(postId: String): Result<Post> {
        return try {
            val document = firestore.collection("posts").document(postId).get().await()
            val post = document.toObject(Post::class.java)
            if (post != null) {
                Result.success(post)
            } else {
                Result.failure(Exception("Gönderi bulunamadı"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun likePost(postId: String, userId: String): Result<Unit> {
        android.util.Log.d("PostRepository", "likePost: postId=$postId, userId=$userId")
        return try {
            firestore.collection("posts").document(postId)
                .update("likes", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
                .await()
            android.util.Log.d("PostRepository", "likePost: success")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("PostRepository", "likePost: failed", e)
            Result.failure(e)
        }
    }

    suspend fun unlikePost(postId: String, userId: String): Result<Unit> {
        android.util.Log.d("PostRepository", "unlikePost: postId=$postId, userId=$userId")
        return try {
            firestore.collection("posts").document(postId)
                .update("likes", com.google.firebase.firestore.FieldValue.arrayRemove(userId))
                .await()
            android.util.Log.d("PostRepository", "unlikePost: success")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("PostRepository", "unlikePost: failed", e)
            Result.failure(e)
        }
    }
    
    suspend fun togglePostLike(postId: String, userId: String): Result<Unit> {
        android.util.Log.d("PostRepository", "togglePostLike: postId=$postId, userId=$userId")
        return try {
            val postRef = firestore.collection("posts").document(postId)
            
            firestore.runTransaction { transaction ->
                val postDoc = transaction.get(postRef)
                val post = postDoc.toObject(Post::class.java)
                
                if (post != null) {
                    val currentLikes = post.likes.toMutableList()
                    
                    if (currentLikes.contains(userId)) {
                        currentLikes.remove(userId)
                        android.util.Log.d("PostRepository", "User $userId unliked post $postId")
                    } else {
                        currentLikes.add(userId)
                        android.util.Log.d("PostRepository", "User $userId liked post $postId")
                    }
                    
                    transaction.update(postRef, "likes", currentLikes)
                }
            }.await()
            
            android.util.Log.d("PostRepository", "Post like toggle successful")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("PostRepository", "Post like toggle failed", e)
            Result.failure(e)
        }
    }

    suspend fun addComment(postId: String, comment: Comment): Result<String> {
        return try {
            val docRef = firestore.collection("posts").document(postId)
                .collection("comments").add(comment).await()
            
            // Ana gönderiye yorum sayısını ekle
            firestore.collection("posts").document(postId)
                .update("comments", com.google.firebase.firestore.FieldValue.arrayUnion(comment))
                .await()
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadMedia(filePath: String, fileName: String): Result<String> {
        return try {
            val storageRef = storage.reference.child("media/$fileName")
            val file = java.io.File(filePath)
            val uploadTask = storageRef.putFile(android.net.Uri.fromFile(file))
            val downloadUrl = uploadTask.await().storage.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadMediaFromUri(uri: android.net.Uri, fileName: String): Result<String> {
        return try {
            val storageRef = storage.reference.child("media/$fileName")
            val uploadTask = storageRef.putFile(uri)
            val downloadUrl = uploadTask.await().storage.downloadUrl.await()
            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadMultipleMedia(mediaUris: List<android.net.Uri>, postId: String, context: Context): Result<List<String>> {
        return try {
            val result = supabaseStorageService.uploadMultipleMedia(context, mediaUris, postId)
            result
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchPosts(query: String): Result<List<Post>> {
        return try {
            val snapshot = firestore.collection("posts")
                .whereArrayContains("tags", query)
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val posts = snapshot.documents.mapNotNull { it.toObject(Post::class.java) }
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPostsByUser(userId: String): Result<List<Post>> {
        return try {
            val snapshot = firestore.collection("posts")
                .whereEqualTo("authorId", userId)
                .get()
                .await()
            
            val posts = snapshot.documents.mapNotNull { it.toObject(Post::class.java) }
                .sortedByDescending { it.createdAt }
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
