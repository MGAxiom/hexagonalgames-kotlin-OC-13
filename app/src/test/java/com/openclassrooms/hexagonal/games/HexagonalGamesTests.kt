package com.openclassrooms.hexagonal.games

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.openclassrooms.hexagonal.games.data.repository.AuthRepository
import com.openclassrooms.hexagonal.games.data.repository.FirestoreRepository
import com.openclassrooms.hexagonal.games.data.repository.PostRepository
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.PostComments
import com.openclassrooms.hexagonal.games.screen.account.AccountViewModel
import com.openclassrooms.hexagonal.games.screen.ad.AddViewModel
import com.openclassrooms.hexagonal.games.screen.ad.FormEvent
import com.openclassrooms.hexagonal.games.screen.details.AddCommentsViewModel
import com.openclassrooms.hexagonal.games.screen.details.PublicationDetailsViewModel
import com.openclassrooms.hexagonal.games.screen.homefeed.HomefeedViewModel
import com.openclassrooms.hexagonal.games.screen.settings.SettingsViewModel
import com.openclassrooms.hexagonal.games.ui.state.AddNavigationEvent
import com.openclassrooms.hexagonal.games.ui.state.NavigationEvent
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class AccountViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AccountViewModel
    private val firebaseUser: FirebaseUser = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mock()
        viewModel = AccountViewModel(authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has current user`() = runTest {
        whenever(authRepository.getCurrentUser()).thenReturn(firebaseUser)
        viewModel = AccountViewModel(authRepository)
        assertEquals(firebaseUser, viewModel.uiState.value.currentUser)
    }

    @Test
    fun `onLogout signs out and triggers navigation`() = runTest {
        viewModel.onLogout()
        verify(authRepository).signOut()
        val uiState = viewModel.uiState.value
        assertNull(uiState.currentUser)
        assertEquals(NavigationEvent.LogoutCompleted, uiState.navigationEvent)
    }

    @Test
    fun `onDeleteAccount success triggers navigation`() = runTest {
        whenever(authRepository.deleteCurrentUserAccount(any(), any())).doAnswer {
            val onSuccess = it.getArgument<() -> Unit>(0)
            onSuccess()
        }

        viewModel.onDeleteAccount()
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertNull(uiState.currentUser)
        assertEquals(NavigationEvent.AccountDeletionCompleted, uiState.navigationEvent)
    }

    @Test
    fun `onDeleteAccount failure shows error`() = runTest {
        val exception = Exception("Deletion failed")
        whenever(authRepository.deleteCurrentUserAccount(any(), any())).doAnswer {
            val onFailure = it.getArgument<(Exception) -> Unit>(1)
            onFailure(exception)
        }

        viewModel.onDeleteAccount()
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals("Deletion failed", uiState.errorMessage)
        assertEquals(NavigationEvent.ShowErrorSnackbar("Deletion failed"), uiState.navigationEvent)
    }

    @Test
    fun `onSignInRequested with no user triggers navigation`() {
        whenever(authRepository.getCurrentUser()).thenReturn(null)
        viewModel.refreshUserState()
        viewModel.onSignInRequested()
        assertEquals(NavigationEvent.RequestActivitySignIn, viewModel.uiState.value.navigationEvent)
    }

    @Test
    fun `onSignInRequested with user does nothing`() {
        whenever(authRepository.getCurrentUser()).thenReturn(firebaseUser)
        viewModel.refreshUserState()
        viewModel.onSignInRequested()
        assertNull(viewModel.uiState.value.navigationEvent)
    }

    @Test
    fun `consumeNavigationEvent clears navigation event`() {
        viewModel.onSignInRequested()
        assertNotNull(viewModel.uiState.value.navigationEvent)
        viewModel.consumeNavigationEvent()
        assertNull(viewModel.uiState.value.navigationEvent)
    }

    @Test
    fun `consumeErrorMessage clears error message`() = runTest {
        val exception = Exception("Test error")
        whenever(authRepository.deleteCurrentUserAccount(any(), any())).doAnswer {
            val onFailure = it.getArgument<(Exception) -> Unit>(1)
            onFailure(exception)
        }
        viewModel.onDeleteAccount()
        testDispatcher.scheduler.advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.errorMessage)
        viewModel.consumeErrorMessage()
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `refreshUserState updates user from repository`() {
        assertNull(viewModel.uiState.value.currentUser)
        whenever(authRepository.getCurrentUser()).thenReturn(firebaseUser)
        viewModel.refreshUserState()
        assertEquals(firebaseUser, viewModel.uiState.value.currentUser)
    }
}

@ExperimentalCoroutinesApi
class AddViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var postRepository: PostRepository
    private lateinit var firestoreRepository: FirestoreRepository
    private lateinit var storage: FirebaseStorage
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AddViewModel
    private val mockUri: Uri = mock()

    private val mockStorageReference: StorageReference = mock()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        postRepository = mock()
        firestoreRepository = mock()
        storage = mock()
        authRepository = mock()
        whenever(storage.reference).thenReturn(mockStorageReference)
        whenever(mockStorageReference.child(any())).thenReturn(mockStorageReference)
        viewModel = AddViewModel(postRepository, firestoreRepository, storage, authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onAction TitleChanged updates post title`() {
        val newTitle = "New Game"
        viewModel.onAction(FormEvent.TitleChanged(newTitle))
        assertEquals(newTitle, viewModel.post.value.title)
    }

    @Test
    fun `onAction DescriptionChanged updates post description`() {
        val newDescription = "A fun game to play"
        viewModel.onAction(FormEvent.DescriptionChanged(newDescription))
        assertEquals(newDescription, viewModel.post.value.description)
    }

    @Test
    fun `onAction PhotoUriChanged updates post photoUri`() {
        viewModel.onAction(FormEvent.PhotoUriChanged(mockUri))
        assertEquals(mockUri, viewModel.post.value.photoUri)
    }

    @Test
    fun `addPost with empty title sets error message`() {
        viewModel.onAction(FormEvent.TitleChanged(""))
        viewModel.addPost()
        assertEquals("Title cannot be empty.", viewModel.uiState.value.errorMessage)
        verify(postRepository, never()).addPost(any())
    }

    @Test
    fun `addPost without image calls repository`() {
        val mockUser: FirebaseUser = mock()
        whenever(mockUser.uid).thenReturn("test-user-id")
        whenever(mockUser.displayName).thenReturn("Maxime")
        whenever(authRepository.getCurrentUser()).thenReturn(mockUser)
        viewModel.onAction(FormEvent.TitleChanged("Test Title"))
        viewModel.addPost()
        val postCaptor = argumentCaptor<Post>()
        verify(postRepository).addPost(postCaptor.capture())
        assertEquals("Test Title", postCaptor.firstValue.title)
        assertEquals("test-user-id", postCaptor.firstValue.author?.id)
        assertEquals("Maxime", postCaptor.firstValue.author?.firstname)
    }

    @Test
    fun `clearSelectedImage sets photoUri to null`() {
        viewModel.onAction(FormEvent.PhotoUriChanged(mockUri))
        assertNotNull(viewModel.post.value.photoUri)
        viewModel.clearSelectedImage()
        assertNull(viewModel.post.value.photoUri)
    }
}

@ExperimentalCoroutinesApi
class AddCommentsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var firestoreRepository: FirestoreRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: AddCommentsViewModel

    private val postId = "test-post-id"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        firestoreRepository = mock()
        authRepository = mock()
        savedStateHandle = SavedStateHandle().apply {
            set("postId", postId)
        }
        viewModel = AddCommentsViewModel(firestoreRepository, authRepository, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `onSaveComment calls repository with correct data`() {
        val commentText = "This is a great post!"
        viewModel.onSaveComment(commentText)

        val postIdCaptor = argumentCaptor<String>()
        val commentCaptor = argumentCaptor<PostComments>()

        verify(firestoreRepository).addCommentToPost(
            postId = postIdCaptor.capture(),
            comment = commentCaptor.capture(),
            onSuccess = any(),
            onFailure = any()
        )

        assertEquals(postId, postIdCaptor.firstValue)
        assertEquals(commentText, commentCaptor.firstValue.comment)
        assertEquals(postId, commentCaptor.firstValue.postId)
        assertNotNull(commentCaptor.firstValue.author)
        assertEquals("Unknown", commentCaptor.firstValue.author?.firstname)
    }
}

@ExperimentalCoroutinesApi
class PublicationDetailsViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var firestoreRepository: FirestoreRepository
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: PublicationDetailsViewModel

    private val postId = "test-post-id"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        firestoreRepository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadPostDetails success updates uiState with post`() = runTest {
        val post = Post(id = postId, title = "Test Post")
        whenever(firestoreRepository.getPost(any(), any(), any())).doAnswer {
            val onSuccess = it.getArgument<(Post) -> Unit>(1)
            onSuccess(post)
        }

        savedStateHandle = SavedStateHandle().apply { set("postId", postId) }
        viewModel = PublicationDetailsViewModel(firestoreRepository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertEquals(post, uiState.post)
        assertEquals(false, uiState.isLoading)
        assertNull(uiState.errorMessage)
    }

    @Test
    fun `loadPostDetails failure updates uiState with error`() = runTest {
        val exception = Exception("Post not found")
        whenever(firestoreRepository.getPost(any(), any(), any())).doAnswer {
            val onFailure = it.getArgument<(Exception) -> Unit>(2)
            onFailure(exception)
        }

        savedStateHandle = SavedStateHandle().apply { set("postId", postId) }
        viewModel = PublicationDetailsViewModel(firestoreRepository, savedStateHandle)
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.uiState.value
        assertNull(uiState.post)
        assertEquals(false, uiState.isLoading)
        assertEquals("Failed to load post details: Post not found", uiState.errorMessage)
    }
}

@ExperimentalCoroutinesApi
class HomefeedViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var postRepository: PostRepository
    private lateinit var firestoreRepository: FirestoreRepository
    private lateinit var viewModel: HomefeedViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        postRepository = mock()
        firestoreRepository = mock()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `getAllPosts combines firestore and local posts`() = runTest {
        val firestorePosts = listOf(Post(id = "1", title = "Firestore Post"))
        val localPosts = listOf(Post(id = "2", title = "Local Post"))

        whenever(firestoreRepository.getAllPosts(any(), any())).doAnswer {
            val onSuccess = it.getArgument<(List<Post>) -> Unit>(0)
            onSuccess(firestorePosts)
        }
        whenever(postRepository.posts).thenReturn(flowOf(localPosts))

        viewModel = HomefeedViewModel(postRepository, firestoreRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        val posts = viewModel.posts.value
        assertEquals(2, posts.size)
        assertEquals(firestorePosts.first(), posts.first())
        assertEquals(localPosts.first(), posts.last())
    }
}