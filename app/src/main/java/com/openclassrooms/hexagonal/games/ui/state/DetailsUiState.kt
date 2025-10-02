package com.openclassrooms.hexagonal.games.ui.state

import com.openclassrooms.hexagonal.games.domain.model.Post

data class DetailsUiState(
    val post: Post? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)