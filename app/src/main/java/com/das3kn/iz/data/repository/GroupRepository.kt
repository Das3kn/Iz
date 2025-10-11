package com.das3kn.iz.data.repository

import com.das3kn.iz.data.model.Group
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val groupsCollection get() = firestore.collection("groups")
    private val usersCollection get() = firestore.collection("users")

    suspend fun createGroup(
        ownerId: String,
        name: String,
        description: String,
        coverImageUrl: String = "",
        invitedUserIds: List<String> = emptyList()
    ): Result<Group> {
        return try {
            val uniqueInvites = invitedUserIds
                .filter { it.isNotBlank() && it != ownerId }
                .distinct()

            val group = Group(
                name = name,
                description = description,
                ownerId = ownerId,
                coverImageUrl = coverImageUrl,
                memberIds = listOf(ownerId),
                pendingInvites = uniqueInvites,
                inviteMetadata = uniqueInvites.associateWith { ownerId }
            )

            val documentRef = groupsCollection.add(group).await()
            val createdGroup = group.copy(id = documentRef.id)
            groupsCollection.document(documentRef.id).set(createdGroup).await()

            usersCollection.document(ownerId)
                .update("groups", FieldValue.arrayUnion(createdGroup.id))
                .await()

            if (uniqueInvites.isNotEmpty()) {
                val batch = firestore.batch()
                uniqueInvites.forEach { inviteeId ->
                    val userRef = usersCollection.document(inviteeId)
                    batch.update(
                        userRef,
                        "groupInvitations",
                        FieldValue.arrayUnion(createdGroup.id)
                    )
                }
                batch.commit().await()
            }

            Result.success(createdGroup)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGroupsForUser(userId: String): Result<List<Group>> {
        return try {
            val snapshot = groupsCollection
                .whereArrayContains("memberIds", userId)
                .get()
                .await()

            val groups = snapshot.documents.mapNotNull { document ->
                document.toObject(Group::class.java)
            }

            Result.success(groups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGroupInvitesForUser(userId: String): Result<List<Group>> {
        return try {
            val snapshot = groupsCollection
                .whereArrayContains("pendingInvites", userId)
                .get()
                .await()

            val groups = snapshot.documents.mapNotNull { document ->
                document.toObject(Group::class.java)
            }

            Result.success(groups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGroupById(groupId: String): Result<Group> {
        return try {
            val document = groupsCollection.document(groupId).get().await()
            val group = document.toObject(Group::class.java)
            if (group != null) {
                Result.success(group)
            } else {
                Result.failure(IllegalArgumentException("Grup bulunamadı"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGroupsByIds(groupIds: List<String>): Result<List<Group>> {
        if (groupIds.isEmpty()) {
            return Result.success(emptyList())
        }

        return try {
            val groups = mutableListOf<Group>()
            groupIds.distinct().chunked(10).forEach { chunk ->
                val snapshot = groupsCollection
                    .whereIn("id", chunk)
                    .get()
                    .await()
                groups += snapshot.documents.mapNotNull { it.toObject(Group::class.java) }
            }
            Result.success(groups)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendGroupInvite(groupId: String, inviterId: String, inviteeId: String): Result<Unit> {
        if (inviteeId.isBlank()) {
            return Result.failure(IllegalArgumentException("Geçersiz davet edilen kullanıcı"))
        }

        return try {
            val groupRef = groupsCollection.document(groupId)

            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(groupRef)
                val group = snapshot.toObject(Group::class.java)
                    ?: throw IllegalArgumentException("Grup bulunamadı")

                if (group.memberIds.contains(inviteeId) || group.pendingInvites.contains(inviteeId)) {
                    return@runTransaction
                }

                transaction.update(
                    groupRef,
                    mapOf(
                        "pendingInvites" to FieldValue.arrayUnion(inviteeId),
                        "inviteMetadata.$inviteeId" to inviterId
                    )
                )
            }.await()

            usersCollection.document(inviteeId)
                .update("groupInvitations", FieldValue.arrayUnion(groupId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun acceptGroupInvite(groupId: String, userId: String): Result<Unit> {
        return try {
            val groupRef = groupsCollection.document(groupId)

            firestore.runTransaction { transaction ->
                transaction.update(
                    groupRef,
                    mapOf(
                        "pendingInvites" to FieldValue.arrayRemove(userId),
                        "memberIds" to FieldValue.arrayUnion(userId),
                        "inviteMetadata.$userId" to FieldValue.delete()
                    )
                )
            }.await()

            usersCollection.document(userId)
                .update(
                    mapOf(
                        "groupInvitations" to FieldValue.arrayRemove(groupId),
                        "groups" to FieldValue.arrayUnion(groupId)
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun declineGroupInvite(groupId: String, userId: String): Result<Unit> {
        return try {
            val groupRef = groupsCollection.document(groupId)

            firestore.runTransaction { transaction ->
                transaction.update(
                    groupRef,
                    mapOf(
                        "pendingInvites" to FieldValue.arrayRemove(userId),
                        "inviteMetadata.$userId" to FieldValue.delete()
                    )
                )
            }.await()

            usersCollection.document(userId)
                .update("groupInvitations", FieldValue.arrayRemove(groupId))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
