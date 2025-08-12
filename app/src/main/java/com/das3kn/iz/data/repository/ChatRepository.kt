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
    
    init {
        // Gerekli index'leri oluştur
        createRequiredIndexes()
    }
    
    private fun createRequiredIndexes() {
        try {
            // Chats collection için composite index
            firestore.collection("chats")
                .whereArrayContains("participants", "temp")
                .orderBy("lastMessageTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener {
                    // Index zaten mevcut
                }
                .addOnFailureListener { exception ->
                    if (exception.message?.contains("FAILED_PRECONDITION") == true) {
                        // Index oluşturulması gerekiyor, kullanıcıya bilgi ver
                        println("Firestore index oluşturulması gerekiyor. Lütfen Firebase Console'da index oluşturun.")
                    }
                }
        } catch (e: Exception) {
            println("Index kontrol hatası: ${e.message}")
        }
    }
    
    suspend fun createChat(participants: List<String>): Result<String> {
        return try {
            // Önce mevcut sohbet var mı kontrol et
            val existingChat = findExistingChat(participants)
            if (existingChat != null) {
                // Mevcut sohbet varsa onun ID'sini döndür
                return Result.success(existingChat.id)
            }
            
            // Yeni sohbet oluştur
            val chat = Chat(
                participants = participants,
                lastMessageTime = System.currentTimeMillis(),
                unreadCount = participants.associateWith { 0 }
            )
            val docRef = firestore.collection("chats").add(chat).await()
            
            // Oluşturulan sohbeti güncelle (ID'yi set et)
            firestore.collection("chats").document(docRef.id).update("id", docRef.id).await()
            
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun findExistingChat(participants: List<String>): Chat? {
        return try {
            // Mevcut sohbetleri kontrol et
            val snapshot = firestore.collection("chats")
                .whereArrayContains("participants", participants.first())
                .get()
                .await()
            
            snapshot.documents.find { doc ->
                val chat = doc.toObject(Chat::class.java)
                chat?.participants?.size == participants.size && 
                chat.participants.containsAll(participants)
            }?.toObject(Chat::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getChats(userId: String): Result<List<Chat>> {
        return try {
            // Geçici olarak index olmadan çalışacak şekilde düzenle
            val snapshot = firestore.collection("chats")
                .whereArrayContains("participants", userId)
                // .orderBy("lastMessageTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val chats = snapshot.documents.mapNotNull { it.toObject(Chat::class.java) }
            // Manuel olarak sırala (index oluşturulana kadar)
            val sortedChats = chats.sortedByDescending { it.lastMessageTime }
            Result.success(sortedChats)
        } catch (e: Exception) {
            if (e.message?.contains("FAILED_PRECONDITION") == true) {
                Result.failure(Exception("Firestore index eksik. Lütfen Firebase Console'da gerekli index'i oluşturun."))
            } else {
                Result.failure(e)
            }
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
                    "lastMessageTime" to message.timestamp
                )
            ).await()
            
            // Sadece diğer katılımcıların okunmamış mesaj sayısını artır
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

    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val document = firestore.collection("users").document(userId).get().await()
            val user = document.toObject(User::class.java)
            if (user != null) {
                Result.success(user)
            } else {
                Result.failure(Exception("Kullanıcı bulunamadı"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUsersByIds(userIds: List<String>): Result<List<User>> {
        return try {
            val users = mutableListOf<User>()
            for (userId in userIds) {
                val userResult = getUserById(userId)
                userResult.onSuccess { user ->
                    users.add(user)
                }
            }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
