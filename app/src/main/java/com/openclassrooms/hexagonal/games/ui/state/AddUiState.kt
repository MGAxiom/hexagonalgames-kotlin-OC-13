package com.openclassrooms.hexagonal.games.ui.state

import android.net.Uri
import com.google.firebase.auth.FirebaseUser

data class AddUiState(
    val navigationEvent: AddNavigationEvent? = null,
    val selectedImageUri: Uri? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val uploadProgress: Int? = null,
)

sealed class AddNavigationEvent {
    data object RequestImagePicker : AddNavigationEvent()
}