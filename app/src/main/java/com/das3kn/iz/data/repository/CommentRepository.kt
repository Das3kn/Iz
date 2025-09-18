package com.das3kn.iz.data.repository

import com.das3kn.iz.data.model.Comment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    // Yorum ekleme
    suspend fun addComment(comment: Comment): Result<Comment> {
        android.util.Log.d("CommentRepository", "addComment: postId=${comment.postId}, content=${comment.content}")
        return try {
            val commentRef = firestore.collection("comments").document()
            val commentWithId = comment.copy(id = commentRef.id)
            
            android.util.Log.d("CommentRepository", "Comment ID generated: ${commentWithId.id}")
            
            commentRef.set(commentWithId).await()
            android.util.Log.d("CommentRepository", "Comment saved to Firestore successfully")
            
            // Post'un comment count'unu güncelle
            updatePostCommentCount(comment.postId, 1)
            
            Result.success(commentWithId)
        } catch (e: Exception) {
            android.util.Log.e("CommentRepository", "addComment failed", e)
            Result.failure(e)
        }
    }
    
    // Post'a ait yorumları getir
    suspend fun getCommentsForPost(postId: String): Result<List<Comment>> {
        return try {
            // Önce sadece postId ile filtrele, sonra memory'de sırala
            val snapshot = firestore.collection("comments")
                .whereEqualTo("postId", postId)
                .get()
                .await()
            
            val allComments = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Comment::class.java)
            }
            
            // Memory'de filtrele ve sırala
            val mainComments = allComments
                .filter { it.parentId == null } // Sadece ana yorumlar
                .sortedByDescending { it.createdAt } // Tarihe göre sırala
            
            // Her yorum için yanıtları getir
            val commentsWithReplies = mainComments.map { comment ->
                comment.copy(replies = getRepliesForComment(comment.id))
            }
            
            android.util.Log.d("CommentRepository", "getCommentsForPost: ${commentsWithReplies.size} comments loaded")
            Result.success(commentsWithReplies)
        } catch (e: Exception) {
            android.util.Log.e("CommentRepository", "getCommentsForPost failed", e)
            Result.failure(e)
        }
    }
    
    // Yorum yanıtlarını getir
    private suspend fun getRepliesForComment(commentId: String): List<Comment> {
        return try {
            val snapshot = firestore.collection("comments")
                .whereEqualTo("parentId", commentId)
                .get()
                .await()
            
            val replies = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Comment::class.java)
            }
            
            // Memory'de sırala
            replies.sortedBy { it.createdAt }
        } catch (e: Exception) {
            android.util.Log.e("CommentRepository", "getRepliesForComment failed", e)
            emptyList()
        }
    }
    
    // Yorum silme
    suspend fun deleteComment(commentId: String, postId: String): Result<Unit> {
        return try {
            firestore.collection("comments").document(commentId).delete().await()
            
            // Post'un comment count'unu güncelle
            updatePostCommentCount(postId, -1)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Yorum beğenme/beğenmeme
    suspend fun toggleCommentLike(commentId: String, userId: String): Result<Unit> {
        android.util.Log.d("CommentRepository", "toggleCommentLike: commentId=$commentId, userId=$userId")
        return try {
            val commentRef = firestore.collection("comments").document(commentId)
            
            firestore.runTransaction { transaction ->
                val commentDoc = transaction.get(commentRef)
                val comment = commentDoc.toObject(Comment::class.java)
                
                if (comment != null) {
                    val currentLikes = comment.likes.toMutableList()
                    
                    if (currentLikes.contains(userId)) {
                        currentLikes.remove(userId)
                        android.util.Log.d("CommentRepository", "User $userId unliked comment $commentId")
                    } else {
                        currentLikes.add(userId)
                        android.util.Log.d("CommentRepository", "User $userId liked comment $commentId")
                    }
                    
                    transaction.update(commentRef, "likes", currentLikes)
                }
            }.await()
            
            android.util.Log.d("CommentRepository", "Comment like toggle successful")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("CommentRepository", "Comment like toggle failed", e)
            Result.failure(e)
        }
    }
    
    // Post comment count güncelleme
    private suspend fun updatePostCommentCount(postId: String, change: Int) {
        try {
            val postRef = firestore.collection("posts").document(postId)
            
            firestore.runTransaction { transaction ->
                val postDoc = transaction.get(postRef)
                val currentCount = postDoc.getLong("commentCount") ?: 0
                transaction.update(postRef, "commentCount", currentCount + change)
            }.await()
        } catch (e: Exception) {
            // Log error but don't fail the main operation
            android.util.Log.e("CommentRepository", "Failed to update comment count", e)
        }
    }
}
