package com.openclassrooms.hexagonal.games.screen.details

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.openclassrooms.hexagonal.games.data.repository.FirestoreRepository
import com.openclassrooms.hexagonal.games.domain.model.PostComments
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.ui.state.DetailsUiState
import com.openclassrooms.hexagonal.games.utils.getCurrentUserName
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

@HiltViewModel
class AddCommentsViewModel @Inject constructor(
    private val firestoreRepository: FirestoreRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()
    val postId: String? = savedStateHandle["postId"]

    fun onSaveComment(commentText: String) {
        val (name, lastname) = getCurrentUserName()
        val author = User(
            "1",
            name ?: "Unknown",
            lastname ?: "Unknown"
        )

        val newComment = PostComments(
            id = UUID.randomUUID().toString(),
            postId = postId.toString(),
            comment = commentText,
            author = author,
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