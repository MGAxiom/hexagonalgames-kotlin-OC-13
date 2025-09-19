package com.openclassrooms.hexagonal.games.screen.ad

import android.widget.ProgressBar
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.ui.state.AddUiState
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    post: Post,
    uiState: AddUiState,
    onFormEvent: (FormEvent) -> Unit,
    onOpenImagePicker: () -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    onClearSelectedImage: () -> Unit,
    onConsumeErrorMessage: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            scope.launch { snackbarHostState.showSnackbar(message) }
            onConsumeErrorMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.add_fragment_label)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.contentDescription_go_back)
                        )
                    }
                },
            )
        }
    ) { contentPadding ->
        CreatePost(
            padding = contentPadding,
            post = post,
            uiState = uiState,
            onFormEvent = onFormEvent,
            onOpenImagePicker = onOpenImagePicker,
            onClearSelectedImage = onClearSelectedImage,
            onSaveClick = onSaveClick
        )
    }
}


@Composable
private fun CreatePost(
    padding: PaddingValues,
    post: Post,
    uiState: AddUiState,
    onFormEvent: (FormEvent) -> Unit,
    onOpenImagePicker: () -> Unit,
    onClearSelectedImage: () -> Unit,
    onSaveClick: () -> Unit,
    error: FormError? = null
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = post.title,
            onValueChange = { onFormEvent(FormEvent.TitleChanged(it)) },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            isError = error is FormError.TitleError
        )
        if (error is FormError.TitleError) {
            Text(
                text = stringResource(id = error.messageRes),
                color = MaterialTheme.colorScheme.error,
            )
        }

        Spacer(Modifier.height(8.dp))

        post.description?.let {
            OutlinedTextField(
                value = it,
                onValueChange = { onFormEvent(FormEvent.DescriptionChanged(it)) },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )
        }
        Spacer(Modifier.height(16.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(CARD_IMAGE_HEIGHT_DP.dp)
                .clickable { onOpenImagePicker() },
        ) {
            if (post.photoUri != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(post.photoUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Selected Post Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onClearSelectedImage,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(8.dp)
                        .background(Color.Black.copy(alpha = 0.5f)),
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear selected image",
                        tint = Color.White
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = stringResource(R.string.action_photo_picker),
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (error is FormError.PhotoError) {
                        Text(
                            text = stringResource(id = error.messageRes),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                    Text(stringResource(R.string.action_photo_picker), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (uiState.uploadProgress != 0) {
            LinearProgressIndicator(
                progress = {
                    uiState.uploadProgress?.let { it / 100f } ?: 0f
                },
                modifier = Modifier.padding(ITEM_PADDING_TOP.dp),
                color = ProgressIndicatorDefaults.linearColor,
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
        }
        Button(
            onClick = onSaveClick,
            enabled = !uiState.isLoading,
            modifier = Modifier
                .padding(ITEM_PADDING_TOP.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.action_save))
        }
    }
}

@PreviewLightDark
//@PreviewScreenSizes
@Composable
private fun CreatePostPreview() {
    HexagonalGamesTheme {
        CreatePost(
            padding = PaddingValues(),
            post = Post(
                id = "1",
                title = "test",
                description = "description",
                photoUrl = null,
                timestamp = System.currentTimeMillis(),
                photoUri = null,
                author = null
            ),
            uiState = AddUiState(),
            onFormEvent = { },
            onOpenImagePicker = { },
            onClearSelectedImage = { },
            onSaveClick = { },
            error = null
        )
    }
}

@PreviewLightDark
//@PreviewScreenSizes
@Composable
private fun CreatePostTitleErrorPreview() {
    HexagonalGamesTheme {
        CreatePost(
            padding = PaddingValues(),
            post = Post(
                id = "1",
                title = "test",
                description = "description",
                photoUrl = null,
                timestamp = System.currentTimeMillis(),
                photoUri = null,
                author = null
            ),
            uiState = AddUiState(),
            onFormEvent = { },
            onOpenImagePicker = { },
            onClearSelectedImage = { },
            onSaveClick = { },
            error = FormError.TitleError
        )
    }
}

@PreviewLightDark
//@PreviewScreenSizes
@Composable
private fun CreatePostImageErrorPreview() {
    HexagonalGamesTheme {
        CreatePost(
            padding = PaddingValues(),
            post = Post(
                id = "1",
                title = "test",
                description = "description",
                photoUrl = null,
                timestamp = System.currentTimeMillis(),
                photoUri = null,
                author = null
            ),
            uiState = AddUiState(),
            onFormEvent = { },
            onOpenImagePicker = { },
            onClearSelectedImage = { },
            onSaveClick = { },
            error = FormError.PhotoError
        )
    }
}

private const val CARD_IMAGE_HEIGHT_DP = 300
private const val ITEM_PADDING_TOP = 16