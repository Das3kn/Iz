package com.das3kn.iz.data.repository

import com.das3kn.iz.data.model.Post
import com.das3kn.iz.data.model.Comment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) {
    
    suspend fun createPost(post: Post): Result<String> {
        return try {
            val docRef = firestore.collection("posts").add(post).await()
            Result.success(docRef.id)
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
        return try {
            firestore.collection("posts").document(postId)
                .update("likes", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unlikePost(postId: String, userId: String): Result<Unit> {
        return try {
            firestore.collection("posts").document(postId)
                .update("likes", com.google.firebase.firestore.FieldValue.arrayRemove(userId))
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
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
}
