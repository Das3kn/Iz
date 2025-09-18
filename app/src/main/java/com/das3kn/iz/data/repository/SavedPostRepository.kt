package com.das3kn.iz.data.repository

import com.das3kn.iz.data.model.Post
import com.das3kn.iz.data.model.SavedPost
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SavedPostRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    suspend fun savePost(userId: String, postId: String): Result<Unit> {
        return try {
            val savedPost = SavedPost(
                userId = userId,
                postId = postId
            )
            
            // SavedPost'u kaydet
            firestore.collection("saved_posts").add(savedPost).await()
            
            // Post'un saves listesine kullanıcıyı ekle
            firestore.collection("posts").document(postId)
                .update("saves", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun unsavePost(userId: String, postId: String): Result<Unit> {
        return try {
            // SavedPost'u sil
            val query = firestore.collection("saved_posts")
                .whereEqualTo("userId", userId)
                .whereEqualTo("postId", postId)
                .get()
                .await()
            
            query.documents.forEach { doc ->
                doc.reference.delete().await()
            }
            
            // Post'un saves listesinden kullanıcıyı çıkar
            firestore.collection("posts").document(postId)
                .update("saves", com.google.firebase.firestore.FieldValue.arrayRemove(userId))
                .await()
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun toggleSavePost(userId: String, postId: String): Result<Unit> {
        return try {
            // Önce kullanıcının bu post'u kaydedip kaydetmediğini kontrol et
            val query = firestore.collection("saved_posts")
                .whereEqualTo("userId", userId)
                .whereEqualTo("postId", postId)
                .get()
                .await()
            
            if (query.isEmpty) {
                // Post kaydedilmemiş, kaydet
                savePost(userId, postId)
            } else {
                // Post kaydedilmiş, kaydı kaldır
                unsavePost(userId, postId)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun isPostSaved(userId: String, postId: String): Result<Boolean> {
        return try {
            val query = firestore.collection("saved_posts")
                .whereEqualTo("userId", userId)
                .whereEqualTo("postId", postId)
                .get()
                .await()
            
            Result.success(!query.isEmpty)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getSavedPosts(userId: String): Result<List<Post>> {
        return try {
            // Kullanıcının kaydettiği post ID'lerini al
            val savedPostsQuery = firestore.collection("saved_posts")
                .whereEqualTo("userId", userId)
                .orderBy("savedAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val savedPostIds = savedPostsQuery.documents.mapNotNull { doc ->
                doc.getString("postId")
            }
            
            if (savedPostIds.isEmpty()) {
                return Result.success(emptyList())
            }
            
            // Kaydedilen post'ları al
            val posts = mutableListOf<Post>()
            for (postId in savedPostIds) {
                val postDoc = firestore.collection("posts").document(postId).get().await()
                val post = postDoc.toObject(Post::class.java)
                if (post != null) {
                    posts.add(post)
                }
            }
            
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
