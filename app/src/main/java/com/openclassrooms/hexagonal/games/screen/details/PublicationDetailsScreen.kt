package com.openclassrooms.hexagonal.games.screen.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.imageLoader
import coil.util.DebugLogger
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.domain.model.PostComments
import com.openclassrooms.hexagonal.games.domain.model.User
import com.openclassrooms.hexagonal.games.screen.ad.FormError
import com.openclassrooms.hexagonal.games.ui.state.DetailsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicationDetailsScreen(
    viewModel: PublicationDetailsViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onAddComment: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(uiState.post?.title ?: "Loading...") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.contentDescription_go_back)
                        )
                    }
                },
            )
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    onAddComment()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(id = R.string.description_button_add)
                )
            }
        }
    ) { contentPadding ->
        uiState.post?.let {
            PostDetails(
                padding = contentPadding,
                post = it,
            )
        }
    }
}

@Composable
private fun PostDetails(
    padding: PaddingValues,
    post: Post,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = post.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                stringResource(
                    id = R.string.details_author,
                    post.author?.firstname ?: "unkown",
                    post.author?.lastname ?: "unkown"
                )
            )
        }
        post.photoUrl?.let {
            AsyncImage(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 8.dp)
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .aspectRatio(ratio = 16 / 9f),
                model = it,
                imageLoader = LocalContext.current.imageLoader.newBuilder()
                    .logger(DebugLogger())
                    .build(),
                placeholder = ColorPainter(Color.DarkGray),
                contentDescription = "image",
                contentScale = ContentScale.Crop,
            )
        }
        post.description?.let {
            Text(
                text = it,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        PostComments(
            postComments = post.comments,
            modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun PostComments(
    postComments: List<PostComments>,
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        modifier = modifier.padding(horizontal = 8.dp)
    )

    Spacer(modifier = Modifier
        .fillMaxWidth()
        .height(10.dp)
    )

    Text(
        text = "Commentaires",
        textAlign = TextAlign.Center,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
    )

    Spacer(modifier = Modifier
        .fillMaxWidth()
        .height(10.dp)
    )

    LazyColumn(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.DarkGray.copy(alpha = 0.2f))
            .fillMaxWidth()
            .padding(8.dp),
    ) {
        items(postComments) { postComment ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.details_author,
                        "${postComment.author?.firstname}",
                        "${postComment.author?.lastname}"
                    )
                )
                Text(postComment.comment)
            }
        }
    }
}

@Preview
@Composable
private fun PostDetailsPreview() {
    PostDetails(
        padding = PaddingValues(),
        post = Post(
            id = "1",
            title = "Un jour d'été",
            description = "This is my description",
            photoUrl = "https://picsum.photos/id/85/1080/",
            timestamp = System.currentTimeMillis(),
            photoUri = null,
            author = User(
                id = "1",
                firstname = "Maxi",
                lastname = "Test",
            )
        ),
    )
}

@Preview
@Composable
private fun PostCommentsPreview() {
    PostComments(
        postComments = listOf(
            PostComments(
                id = "1",
                postId = "",
                author = User(
                    id = "1",
                    firstname = "Maxi",
                    lastname = "Test",
                ),
                comment = "This is my comment",
            ),
            PostComments(
                id = "1",
                postId = "",
                author = User(
                    id = "1",
                    firstname = "Maxi",
                    lastname = "Test",
                ),
                comment = "This is my comment",
            ),
            PostComments(
                id = "1",
                postId = "",
                author = User(
                    id = "1",
                    firstname = "Maxi",
                    lastname = "Test",
                ),
                comment = "This is my comment",
            )
        )
    )
}