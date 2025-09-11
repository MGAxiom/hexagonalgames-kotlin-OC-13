package com.openclassrooms.hexagonal.games.screen.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.AuthRepository
import com.openclassrooms.hexagonal.games.ui.state.AccountUiState
import com.openclassrooms.hexagonal.games.ui.state.NavigationEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AccountUiState(currentUser = authRepository.getCurrentUser()))
    val uiState: StateFlow<AccountUiState> = _uiState.asStateFlow()

    init {
        // If you had a reactive way to get user updates from AuthRepository, you'd collect it here
        // For example:
        // viewModelScope.launch {
        //     authRepository.getAuthStateFlow().collect { user -> // Assuming AuthRepository exposes this
        //         _uiState.update { it.copy(currentUser = user) }
        //     }
        // }
        // For now, refreshUser() can be called if needed, or rely on MainActivity's global listener.
    }

    fun onLogout() {
        _uiState.update { it.copy(isLoading = true) }
        authRepository.signOut() // This will trigger MainActivity's AuthStateListener
        _uiState.update {
            it.copy(
                isLoading = false,
                currentUser = null, // Reflect immediately in this VM's state
                navigationEvent = NavigationEvent.LogoutCompleted
            )
        }
    }

    fun onDeleteAccount() {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            authRepository.deleteCurrentUserAccount(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentUser = null, // Reflect immediately
                            navigationEvent = NavigationEvent.AccountDeletionCompleted
                        )
                    }
                },
                onFailure = { exception ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = exception.message ?: "Failed to delete account.",
                            navigationEvent = NavigationEvent.ShowErrorSnackbar(exception.message ?: "Failed to delete account.")
                        )
                    }
                }
            )
        }
    }

    fun onSignInRequested() {
        if (_uiState.value.currentUser == null) {
            _uiState.update { it.copy(navigationEvent = NavigationEvent.RequestActivitySignIn) }
        }
    }

    fun consumeNavigationEvent() {
        _uiState.update { it.copy(navigationEvent = null) }
    }

    fun consumeErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun refreshUserState() {
        _uiState.update { it.copy(currentUser = authRepository.getCurrentUser()) }
    }
}