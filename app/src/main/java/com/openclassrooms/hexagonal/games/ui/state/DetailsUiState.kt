package com.openclassrooms.hexagonal.games.ui.state

import android.net.Uri

data class DetailsUiState(
//    val navigationEvent: AddNavigationEvent? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val uploadProgress: Int? = null,
)