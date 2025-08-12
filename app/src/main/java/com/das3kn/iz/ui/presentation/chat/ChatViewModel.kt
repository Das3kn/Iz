package com.das3kn.iz.ui.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das3kn.iz.data.model.Chat
import com.das3kn.iz.data.model.Message
import com.das3kn.iz.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: com.das3kn.iz.data.repository.AuthRepository
) : ViewModel() {

    private val _chatState = MutableStateFlow<Chat?>(null)
    val chatState: StateFlow<Chat?> = _chatState.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _chatUsers = MutableStateFlow<List<com.das3kn.iz.data.model.User>>(emptyList())
    val chatUsers: StateFlow<List<com.das3kn.iz.data.model.User>> = _chatUsers.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadChat(chatId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = chatRepository.getChatById(chatId)
                result.onSuccess { chat ->
                    _chatState.value = chat
                    
                    // Sohbet katılımcılarının bilgilerini al
                    loadChatUsers(chat.participants)
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Sohbet yüklenemedi"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Bilinmeyen hata"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadChatUsers(participantIds: List<String>) {
        try {
            val usersResult = chatRepository.getUsersByIds(participantIds)
            usersResult.onSuccess { users ->
                _chatUsers.value = users
            }
        } catch (e: Exception) {
            println("Kullanıcı bilgileri yüklenemedi: ${e.message}")
        }
    }

    fun loadMessages(chatId: String) {
        viewModelScope.launch {
            try {
                val result = chatRepository.getMessages(chatId)
                result.onSuccess { messageList ->
                    _messages.value = messageList
                    
                    // Mesajlar yüklendiğinde unread count'u sıfırla
                    val currentUser = authRepository.currentUser
                    if (currentUser != null) {
                        chatRepository.markMessagesAsRead(chatId, currentUser.uid)
                    }
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Mesajlar yüklenemedi"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Bilinmeyen hata"
            }
        }
    }

    fun sendMessage(chatId: String, content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            try {
                val currentUser = authRepository.currentUser
                if (currentUser != null) {
                    val message = Message(
                        chatId = chatId,
                        content = content,
                        senderId = currentUser.uid,
                        senderName = currentUser.displayName ?: currentUser.email ?: "Kullanıcı"
                    )

                    val result = chatRepository.sendMessage(chatId, message)
                    result.onSuccess { messageId ->
                        // Mesaj başarıyla gönderildi, UI'da güncelleme yapılabilir
                        // Mesajları yeniden yükle
                        loadMessages(chatId)
                    }.onFailure { exception ->
                        _error.value = exception.message ?: "Mesaj gönderilemedi"
                    }
                } else {
                    _error.value = "Kullanıcı giriş yapmamış"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Bilinmeyen hata"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun getCurrentUserId(): String? {
        return authRepository.currentUser?.uid
    }
}
