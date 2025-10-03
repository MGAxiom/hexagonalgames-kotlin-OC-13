package com.openclassrooms.hexagonal.games.screen.details


import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.openclassrooms.hexagonal.games.data.repository.FirestoreRepository
import com.openclassrooms.hexagonal.games.ui.state.DetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PublicationDetailsViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()
    val postId: String? = savedStateHandle["postId"]

    init {
        if (postId != null) {
            loadPostDetails(postId)
        }
    }

    internal fun refreshDetails() {
        _uiState.value.post?.id?.let { loadPostDetails(it) }
    }

    private fun loadPostDetails(postId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                firestoreRepository.getPost(
                    postId = postId,
                    onSuccess = { post ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                post = post
                            )
                        }
                    },
                    onFailure = { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Failed to load post details: ${exception.message}"
                            )
                        }
                    }
                )
            } catch (error: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load post details, $error occured."
                    )
                }
            }
        }
    }
}

