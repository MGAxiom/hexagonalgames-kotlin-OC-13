package com.openclassrooms.hexagonal.games.ui.state

import android.net.Uri
import com.google.firebase.auth.FirebaseUser

data class AddUiState(
    val navigationEvent: NavigationEvent? = null,
    val selectedImageUri: Uri? = null
)

sealed class AddNavigationEvent {
    data object RequestImagePicker : NavigationEvent()
}