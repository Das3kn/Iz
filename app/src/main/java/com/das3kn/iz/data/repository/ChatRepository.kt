package com.das3kn.iz.data.repository

import com.das3kn.iz.data.model.Chat
import com.das3kn.iz.data.model.Message
import com.das3kn.iz.data.model.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    suspend fun createChat(participants: List<String>): Result<String> {
        return try {
            val chat = Chat(participants = participants)
            val docRef = firestore.collection("chats").add(chat).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChats(userId: String): Result<List<Chat>> {
        return try {
            val snapshot = firestore.collection("chats")
                .whereArrayContains("participants", userId)
                .orderBy("lastMessageTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val chats = snapshot.documents.mapNotNull { it.toObject(Chat::class.java) }
            Result.success(chats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getChatById(chatId: String): Result<Chat> {
        return try {
            val document = firestore.collection("chats").document(chatId).get().await()
            val chat = document.toObject(Chat::class.java)
            if (chat != null) {
                Result.success(chat)
            } else {
                Result.failure(Exception("Sohbet bulunamadı"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(chatId: String, message: Message): Result<String> {
        return try {
            val docRef = firestore.collection("chats").document(chatId)
                .collection("messages").add(message).await()
            
            // Ana sohbeti güncelle
            firestore.collection("chats").document(chatId).update(
                mapOf(
                    "lastMessage" to message,
                    "lastMessageTime" to message.timestamp,
                    "unreadCount.${message.senderId}" to 0
                )
            ).await()
            
            // Diğer katılımcıların okunmamış mesaj sayısını artır
            val chat = getChatById(chatId).getOrNull()
            chat?.participants?.filter { it != message.senderId }?.forEach { participantId ->
                firestore.collection("chats").document(chatId)
                    .update("unreadCount.$participantId", 
                        com.google.firebase.firestore.FieldValue.increment(1))
                    .await()
            }
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getMessages(chatId: String, limit: Int = 50): Result<List<Message>> {
        return try {
            val snapshot = firestore.collection("chats").document(chatId)
                .collection("messages")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            
            val messages = snapshot.documents.mapNotNull { it.toObject(Message::class.java) }
            Result.success(messages.reversed())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun listenToMessages(chatId: String, onMessage: (Message) -> Unit): ListenerRegistration {
        return firestore.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.documents?.forEach { document ->
                    document.toObject(Message::class.java)?.let { message ->
                        onMessage(message)
                    }
                }
            }
    }

    suspend fun markMessagesAsRead(chatId: String, userId: String): Result<Unit> {
        return try {
            firestore.collection("chats").document(chatId)
                .update("unreadCount.$userId", 0)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            val snapshot = firestore.collection("users")
                .whereGreaterThanOrEqualTo("username", query)
                .whereLessThanOrEqualTo("username", query + '\uf8ff')
                .limit(10)
                .get()
                .await()
            
            val users = snapshot.documents.mapNotNull { it.toObject(User::class.java) }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
