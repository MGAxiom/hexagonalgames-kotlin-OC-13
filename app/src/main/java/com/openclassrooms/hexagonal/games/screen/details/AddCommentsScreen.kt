package com.openclassrooms.hexagonal.games.screen.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.domain.model.Post
import com.openclassrooms.hexagonal.games.ui.state.DetailsUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCommentsScreen(
    onBackClick: () -> Unit,
    onSaveComment: () -> Unit,
) {

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_comment_title)) },
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
    ) { contentPadding ->
        AddCommentsContent(
            padding = contentPadding,
            onSaveClick = onSaveComment
        )
    }
}

@Composable
private fun AddCommentsContent(
    padding: PaddingValues,
    onSaveClick: () -> Unit,
) {
    var text = remember { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(padding)
            .fillMaxWidth()
            .padding(ITEM_PADDING_TOP.dp),
    ) {
        OutlinedTextField(
            value = text.value,
            onValueChange = { text.value = it },
            label = { Text("Comment") },
            modifier = Modifier.fillMaxWidth().height(COMMENT_TEXTFIELD_SIZE.dp),
            maxLines = 20
        )
        
        Button(
            onClick = onSaveClick,
            enabled = text.value.isNotEmpty() && text.value.isNotBlank(),
            modifier = Modifier
                .padding(ITEM_PADDING_TOP.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.action_save))
        }
    }
}

@Preview
@Composable
private fun AddCommentsScreenPreview() {
    AddCommentsScreen(
        onBackClick = {},
        onSaveComment = {}
    )
}

private const val ITEM_PADDING_TOP = 16
private const val COMMENT_TEXTFIELD_SIZE = 340