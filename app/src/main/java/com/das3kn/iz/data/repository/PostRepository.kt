package com.das3kn.iz.data.repository

import com.das3kn.iz.data.model.Post
import com.das3kn.iz.data.model.Comment
import com.das3kn.iz.data.model.User
import com.das3kn.iz.data.supabase.SupabaseStorageService
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
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
            val resolvedPosts = resolveReposts(posts)
            android.util.Log.d("PostRepository", "Retrieved ${posts.size} posts")
            posts.forEach { post ->
                android.util.Log.d("PostRepository", "Post ${post.id} has ${post.mediaUrls.size} media URLs: ${post.mediaUrls}")
            }
            Result.success(resolvedPosts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getPostById(postId: String): Result<Post> {
        return try {
            val document = firestore.collection("posts").document(postId).get().await()
            val post = document.toObject(Post::class.java)?.let { resolveReposts(listOf(it)).first() }
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
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val posts = snapshot.documents.mapNotNull { it.toObject(Post::class.java) }
                .let { resolveReposts(it) }
                .sortedByDescending { it.createdAt }
            Result.success(posts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun resolveReposts(posts: List<Post>): List<Post> {
        if (posts.isEmpty()) return posts

        val repostIds = posts.mapNotNull { it.repostOfPostId }.distinct().filter { it.isNotBlank() }
        if (repostIds.isEmpty()) return posts

        val originals = mutableMapOf<String, Post>()
        for (originalId in repostIds) {
            try {
                val document = firestore.collection("posts").document(originalId).get().await()
                document.toObject(Post::class.java)?.let { original ->
                    originals[originalId] = original
                }
            } catch (e: Exception) {
                android.util.Log.e("PostRepository", "resolveReposts: failed to fetch original $originalId", e)
            }
        }

        return posts.map { post ->
            val original = post.repostOfPostId?.let { originals[it] }
            if (original != null) {
                post.copy(originalPost = original)
            } else {
                post
            }
        }
    }

    suspend fun createRepost(originalPost: Post, user: User): Result<Post> {
        return try {
            val repostingUserId = user.id
            if (repostingUserId.isBlank()) {
                return Result.failure(IllegalArgumentException("Kullanıcı bilgileri eksik"))
            }

            val existingRepostQuery = firestore.collection("posts")
                .whereEqualTo("repostOfPostId", originalPost.id)
                .whereEqualTo("repostedByUserId", repostingUserId)
                .get()
                .await()

            if (!existingRepostQuery.isEmpty) {
                val existing = existingRepostQuery.documents.first().toObject(Post::class.java)
                if (existing != null) {
                    return Result.success(existing.copy(originalPost = originalPost))
                }
            }

            val displayName = user.displayName.ifBlank {
                user.username.ifBlank { repostingUserId }
            }

            val repost = Post(
                userId = repostingUserId,
                username = displayName,
                userProfileImage = user.profileImageUrl,
                content = "",
                mediaUrls = emptyList(),
                mediaType = originalPost.mediaType,
                likes = emptyList(),
                comments = emptyList(),
                commentCount = 0,
                shares = 0,
                saves = emptyList(),
                createdAt = System.currentTimeMillis(),
                tags = emptyList(),
                category = originalPost.category,
                repostOfPostId = originalPost.id,
                repostedByUserId = repostingUserId,
                repostedByUsername = user.username,
                repostedByDisplayName = displayName,
                repostedByProfileImage = user.profileImageUrl,
                repostedAt = System.currentTimeMillis()
            )

            val docRef = firestore.collection("posts").add(repost).await()
            val savedRepost = repost.copy(id = docRef.id, originalPost = originalPost)
            firestore.collection("posts").document(docRef.id).set(savedRepost.copy(originalPost = null)).await()

            try {
                firestore.collection("posts").document(originalPost.id)
                    .update("shares", FieldValue.increment(1))
                    .await()
            } catch (e: Exception) {
                android.util.Log.e("PostRepository", "createRepost: failed to increment share count", e)
            }

            Result.success(savedRepost)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
