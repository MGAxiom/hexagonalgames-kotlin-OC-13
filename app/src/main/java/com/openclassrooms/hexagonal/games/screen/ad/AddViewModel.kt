package com.openclassrooms.hexagonal.games.screen.ad

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.openclassrooms.hexagonal.games.data.repository.FirestoreRepository
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.ui.state.AddNavigationEvent
import com.openclassrooms.hexagonal.games.ui.state.AddUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject

/**
 * This ViewModel manages data and interactions related to adding new posts in the AddScreen.
 * It utilizes dependency injection to retrieve a PostRepository instance for interacting with post data.
 */
@HiltViewModel
class AddViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val firestorage: FirestoreRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddUiState())
    val uiState: StateFlow<AddUiState> = _uiState.asStateFlow()

    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
    private val storageRef: StorageReference = storage.reference

    /**
     * Internal mutable state flow representing the current post being edited.
     */
    private var _post = MutableStateFlow(
        Post(
            id = UUID.randomUUID().toString(),
            title = "",
            description = "",
            photoUrl = null,
            timestamp = System.currentTimeMillis(),
            photoUri = null,
            author = null
        )
    )

    /**
     * Public state flow representing the current post being edited.
     * This is immutable for consumers.
     */
    val post: StateFlow<Post>
        get() = _post

    /**
     * StateFlow derived from the post that emits a FormError if the title is empty, null otherwise.
     */
    val error = post.map {
        verifyPost()
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    /**
     * Handles form events like title and description changes.
     *
     * @param formEvent The form event to be processed.
     */
    fun onAction(formEvent: FormEvent) {
        when (formEvent) {
            is FormEvent.DescriptionChanged -> {
                _post.value = _post.value.copy(
                    description = formEvent.description
                )
            }

            is FormEvent.TitleChanged -> {
                _post.value = _post.value.copy(
                    title = formEvent.title
                )
            }

            is FormEvent.AuthorChanged -> {
                _post.value = _post.value.copy(
                    author = formEvent.author
                )
            }

            is FormEvent.PhotoUriChanged -> {
                _post.value = _post.value.copy(
                    photoUri = formEvent.photoUri
                )
            }
        }
    }

    fun onOpenImagePickerRequested() {
        _uiState.update { it.copy(navigationEvent = AddNavigationEvent.RequestImagePicker) }
    }

    /**
     * Attempts to add the current post to the repository after setting the author.
     *
     */
    fun addPost() {
        val currentPost = _post.value
        if (verifyPost() != null) {
            _uiState.update { it.copy(errorMessage = "Title cannot be empty.") }
            return
        }

        val (name, lastname) = getCurrentUserName()
        val author = User(
            "1",
            name ?: "Unknown",
            lastname ?: "Unknown"
        )

        if (currentPost.photoUri != null) {
            uploadImageAndAddPost(currentPost.photoUri, currentPost, author)
        } else {
            val postToSave =
                currentPost.copy(author = author, timestamp = System.currentTimeMillis())
            postRepository.addPost(postToSave)

            // TODO: Update UIState to indicate success/navigate back
            // _uiState.update { it.copy(navigationEvent = SomeSuccessEvent) }
        }
    }

    private fun uploadImageAndAddPost(imageUri: Uri, postData: Post, author: User) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val userId = author.id
        val fileName =
            "post_images/${userId}/${UUID.randomUUID()}_${imageUri.lastPathSegment ?: "image.jpg"}"
        val imageStorageRef = storageRef.child(fileName)

        val uploadTask = imageStorageRef.putFile(imageUri)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            imageStorageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                val downloadUrlString = downloadUri.toString()

                val postWithImageUrl = postData.copy(
                    photoUrl = downloadUrlString,
                    photoUri = null,
                    author = author,
                    timestamp = System.currentTimeMillis()
                )
                postRepository.addPost(postWithImageUrl)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Post added successfully!"
                    )
                }
                firestorage.addPost(
                    postWithImageUrl,
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Post added successfully!"
                            )
                        }
                        Log.d("AddViewModel", "addPost = success!")
                    },
                    onFailure = {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Error adding post: ${it.errorMessage}"
                            )
                        }
                        Log.d("AddViewModel", "addPost = eh it failed!")
                    }
                )
                // TODO: Update UIState to indicate success/navigate back
            }.addOnFailureListener { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Image upload succeeded but failed to get URL: ${exception.message}"
                    )
                }
            }
        }.addOnFailureListener { exception ->
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Image upload failed: ${exception.message}"
                )
            }
        }.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
            _uiState.update { it.copy(uploadProgress = progress.toInt()) }
        }
    }

    fun clearSelectedImage() {
        _post.update { it.copy(photoUri = null) }
    }

    fun consumeNavigationEvent() {
        _uiState.update { it.copy(navigationEvent = null) }
    }

    fun consumeErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun getCurrentUserName(): Pair<String?, String?> {
        val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        var firstName: String? = null
        var lastName: String? = null

        firebaseUser?.displayName?.let { fullName ->
            if (fullName.isNotBlank()) {
                val parts = fullName.split(" ", limit = 2)
                firstName = parts.getOrNull(0)
                if (parts.size > 1) {
                    lastName = parts.getOrNull(1)
                } else {
                    Log.w(
                        "UserInfo",
                        "Display name '$fullName' might not contain a separate last name."
                    )
                }
            }
        }
        return Pair(firstName, lastName)
    }


    /**
     * Verifies mandatory fields of the post
     * and returns a corresponding FormError if so.
     *
     * @return A FormError.TitleError if title is empty, null otherwise.
     */
    private fun verifyPost(): FormError? {
        return if (_post.value.title.isEmpty()) {
            FormError.TitleError
        } else {
            null
        }
    }

}
