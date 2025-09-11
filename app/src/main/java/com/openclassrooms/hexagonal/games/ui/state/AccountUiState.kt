package com.openclassrooms.hexagonal.games.ui.state


import com.google.firebase.auth.FirebaseUser

data class AccountUiState(
    val currentUser: FirebaseUser? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val navigationEvent: NavigationEvent? = null
)

sealed class NavigationEvent {
    data object NavigateToLogin : NavigationEvent()
    data object LogoutCompleted : NavigationEvent()
    data object AccountDeletionCompleted : NavigationEvent()
    data class ShowErrorSnackbar(val message: String) : NavigationEvent()
    data object RequestActivitySignIn : NavigationEvent()
}
