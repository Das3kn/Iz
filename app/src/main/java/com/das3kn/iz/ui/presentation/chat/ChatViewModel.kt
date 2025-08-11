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
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _chatState = MutableStateFlow<Chat?>(null)
    val chatState: StateFlow<Chat?> = _chatState.asStateFlow()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

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

    fun loadMessages(chatId: String) {
        viewModelScope.launch {
            try {
                val result = chatRepository.getMessages(chatId)
                result.onSuccess { messageList ->
                    _messages.value = messageList
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
                val message = Message(
                    chatId = chatId,
                    content = content,
                    senderId = "currentUserId", // TODO: Get from AuthRepository
                    senderName = "Kullanıcı" // TODO: Get from AuthRepository
                )

                val result = chatRepository.sendMessage(chatId, message)
                result.onSuccess { messageId ->
                    // Mesaj başarıyla gönderildi, UI'da güncelleme yapılabilir
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Mesaj gönderilemedi"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Bilinmeyen hata"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
