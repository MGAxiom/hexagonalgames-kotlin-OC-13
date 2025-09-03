package com.openclassrooms.hexagonal.games.screen.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AccountViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _logoutEvent = MutableSharedFlow<Unit>()
    val logoutEvent = _logoutEvent.asSharedFlow()

    private val _accountDeletedEvent = MutableSharedFlow<Unit>()
    val accountDeletedEvent = _accountDeletedEvent.asSharedFlow()

    private val _errorEvent = MutableSharedFlow<String>()
    val errorEvent = _errorEvent.asSharedFlow()

    fun onLogout() {
        authRepository.signOut()
        viewModelScope.launch {
            _logoutEvent.emit(Unit)
        }
    }

    fun onDeleteAccount() {
        viewModelScope.launch {
            authRepository.deleteCurrentUserAccount(
                onSuccess = {
                    viewModelScope.launch {
                        _accountDeletedEvent.emit(Unit)
                    }
                },
                onFailure = { exception ->
                    viewModelScope.launch {
                        _errorEvent.emit(exception.message ?: "Failed to delete account.")
                    }
                }
            )
        }
    }

}