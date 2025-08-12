package com.das3kn.iz.ui.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.das3kn.iz.data.model.User
import com.das3kn.iz.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val authRepository: com.das3kn.iz.data.repository.AuthRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<com.das3kn.iz.data.model.User>>(emptyList())
    val searchResults: StateFlow<List<com.das3kn.iz.data.model.User>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _shouldNavigateBack = MutableStateFlow(false)
    val shouldNavigateBack: StateFlow<Boolean> = _shouldNavigateBack.asStateFlow()

    private val _createdChatId = MutableStateFlow<String?>(null)
    val createdChatId: StateFlow<String?> = _createdChatId.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _error.value = null
    }

    fun searchUsers(query: String) {
        if (query.length < 2) return
        
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            try {
                val result = chatRepository.searchUsers(query)
                result.onSuccess { users ->
                    _searchResults.value = users
                }.onFailure { exception ->
                    _error.value = exception.message ?: "Kullanıcı arama hatası"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Kullanıcı arama hatası"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun startChat(userId: String) {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.currentUser
                if (currentUser != null) {
                    // Mevcut kullanıcı ve seçilen kullanıcının ID'lerini birleştir
                    val participants = listOf(currentUser.uid, userId).distinct()
                    
                    val result = chatRepository.createChat(participants)
                    result.onSuccess { chatId ->
                        // Sohbet başarıyla oluşturuldu
                        _error.value = null
                        _createdChatId.value = chatId
                        // Başarı mesajı göster
                        _successMessage.value = "Sohbet başarıyla oluşturuldu!"
                        // Kısa süre sonra mesajı temizle
                        kotlinx.coroutines.delay(2000)
                        _successMessage.value = null
                        _shouldNavigateBack.value = true
                    }.onFailure { exception ->
                        _error.value = exception.message ?: "Sohbet başlatma hatası"
                    }
                } else {
                    _error.value = "Kullanıcı giriş yapmamış"
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Sohbet başlatma hatası"
            }
        }
    }
}
