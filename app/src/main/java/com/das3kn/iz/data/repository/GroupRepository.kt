package com.das3kn.iz.data.repository

import com.das3kn.iz.data.model.Group
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val groupsCollection get() = firestore.collection("groups")

    suspend fun getGroups(): Result<List<Group>> {
        return try {
            val snapshot = groupsCollection.get().await()
            val groups = snapshot.documents.mapNotNull { document ->
                document.toObject(Group::class.java)?.copy(id = document.id)
            }
            Result.success(groups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGroupById(groupId: String): Result<Group> {
        return try {
            val document = groupsCollection.document(groupId).get().await()
            val group = document.toObject(Group::class.java)?.copy(id = document.id)
            if (group != null) {
                Result.success(group)
            } else {
                Result.failure(IllegalStateException("Grup bulunamadı"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun observeGroup(groupId: String): Flow<Result<Group>> = callbackFlow {
        val registration = groupsCollection.document(groupId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Result.failure(error))
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val group = snapshot.toObject(Group::class.java)?.copy(id = snapshot.id)
                if (group != null) {
                    trySend(Result.success(group))
                }
            }
        }
        awaitClose { registration.remove() }
    }

    suspend fun updateGroup(groupId: String, updates: Map<String, Any?>): Result<Unit> {
        return try {
            groupsCollection.document(groupId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun setGroupPrivacy(groupId: String, isPrivate: Boolean): Result<Unit> {
        return updateGroup(groupId, mapOf("isPrivate" to isPrivate))
    }

    suspend fun createGroup(group: Group): Result<String> {
        return try {
            val documentRef = groupsCollection.add(group.copy(id = "")).await()
            val id = documentRef.id
            groupsCollection.document(id).set(group.copy(id = id)).await()
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGroup(groupId: String): Result<Unit> {
        return try {
            groupsCollection.document(groupId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
