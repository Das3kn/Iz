package com.das3kn.iz.data.repository

import com.das3kn.iz.data.model.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val usersCollection get() = firestore.collection("users")

    suspend fun searchUsers(query: String, limit: Long = 20): Result<List<User>> {
        if (query.isBlank()) {
            return Result.success(emptyList())
        }

        return try {
            val snapshot = usersCollection
                .limit(100)
                .get()
                .await()

            val normalizedQuery = query.trim()
            val users = snapshot.documents
                .mapNotNull { it.toObject(User::class.java) }
                .filter { user ->
                    user.displayName.contains(normalizedQuery, ignoreCase = true) ||
                        user.username.contains(normalizedQuery, ignoreCase = true)
                }
                .distinctBy { it.id }
                .take(limit.toInt())

            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val document = usersCollection.document(userId).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(IllegalArgumentException("Kullanıcı bulunamadı"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsersByIds(userIds: List<String>): Result<List<User>> {
        if (userIds.isEmpty()) {
            return Result.success(emptyList())
        }

        return try {
            val users = mutableListOf<User>()
            userIds.chunked(10).forEach { chunk ->
                val snapshot = usersCollection.whereIn("id", chunk).get().await()
                users += snapshot.documents.mapNotNull { it.toObject(User::class.java) }
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendFriendRequest(fromUserId: String, toUserId: String): Result<Unit> {
        if (fromUserId == toUserId) {
            return Result.failure(IllegalArgumentException("Kendinize arkadaşlık isteği gönderemezsiniz"))
        }

        return try {
            val batch = firestore.batch()
            val fromRef = usersCollection.document(fromUserId)
            val toRef = usersCollection.document(toUserId)

            batch.update(
                fromRef,
                mapOf("outgoingFriendRequests" to FieldValue.arrayUnion(toUserId))
            )
            batch.update(
                toRef,
                mapOf("incomingFriendRequests" to FieldValue.arrayUnion(fromUserId))
            )

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptFriendRequest(currentUserId: String, requesterId: String): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val currentRef = usersCollection.document(currentUserId)
            val requesterRef = usersCollection.document(requesterId)

            batch.update(
                currentRef,
                mapOf(
                    "incomingFriendRequests" to FieldValue.arrayRemove(requesterId),
                    "friends" to FieldValue.arrayUnion(requesterId)
                )
            )

            batch.update(
                requesterRef,
                mapOf(
                    "outgoingFriendRequests" to FieldValue.arrayRemove(currentUserId),
                    "friends" to FieldValue.arrayUnion(currentUserId)
                )
            )

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun declineFriendRequest(currentUserId: String, requesterId: String): Result<Unit> {
        return try {
            val batch = firestore.batch()
            val currentRef = usersCollection.document(currentUserId)
            val requesterRef = usersCollection.document(requesterId)

            batch.update(
                currentRef,
                mapOf("incomingFriendRequests" to FieldValue.arrayRemove(requesterId))
            )

            batch.update(
                requesterRef,
                mapOf("outgoingFriendRequests" to FieldValue.arrayRemove(currentUserId))
            )

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

