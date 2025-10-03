package com.openclassrooms.hexagonal.games.screen.details

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.openclassrooms.hexagonal.games.data.repository.AuthRepository
import com.openclassrooms.hexagonal.games.data.repository.FirestoreRepository
import com.openclassrooms.hexagonal.games.domain.model.PostComments
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.ui.state.DetailsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

@HiltViewModel
class AddCommentsViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    private val authRepository: AuthRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()
    val postId: String? = savedStateHandle["postId"]

    fun onSaveComment(commentText: String) {
        val firebaseUser = authRepository.getCurrentUser()

        val currentUser = User(
            id = firebaseUser?.uid ?: "unknown_id",
            firstname = firebaseUser?.displayName ?: "Unknown",
            lastname = ""
        )

        val newComment = PostComments(
            id = UUID.randomUUID().toString(),
            postId = postId.toString(),
            comment = commentText,
            author = currentUser,
        )


        postId.let {
            firestoreRepository.addCommentToPost(
                postId = it.toString(),
                comment = newComment,
                onSuccess = {
                    Log.d("AddCommentVM", "Comment added successfully!")
                },
                onFailure = { exception ->
                    Log.e("AddCommentVM", "Failed to add comment", exception)
                }
            )
        }
    }
}