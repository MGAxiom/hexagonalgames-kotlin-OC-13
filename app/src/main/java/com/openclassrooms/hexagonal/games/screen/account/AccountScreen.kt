package com.openclassrooms.hexagonal.games.screen.account

import com.openclassrooms.hexagonal.games.screen.settings.SettingsViewModel

import android.os.Build
import androidx.activity.result.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.ui.state.AccountUiState
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    uiState: AccountUiState,
    onConsumeError: () -> Unit,
    onBackClick: () -> Unit,
    onSignInRequested: () -> Unit,
    onLogoutClicked: () -> Unit,
    onDeleteAccountClicked: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            scope.launch { snackbarHostState.showSnackbar(message) }
            onConsumeError()
        }
    }
    LaunchedEffect(Unit) {
        onRefresh()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(id = R.string.action_account))
                },
                navigationIcon = {
                    IconButton(onClick = {
                        onBackClick()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.contentDescription_go_back)
                        )
                    }
                }
            )
        }
    ) { contentPadding ->
        AccountElements(
            modifier = Modifier.padding(contentPadding),
            onDeleteClick = onDeleteAccountClicked,
            onLogoutClicked = onLogoutClicked,
            uiState = uiState
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun AccountElements(
    uiState: AccountUiState,
    modifier: Modifier = Modifier,
    onLogoutClicked: () -> Unit,
    onDeleteClick: () -> Unit
) {

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Signed in as: ${uiState.currentUser?.email ?: uiState.currentUser?.uid}"
        )
        Button(
            onClick = { onDeleteClick() }
        ) {
            Text(text = stringResource(id = R.string.delete_account_button))
        }
        Button(
            onClick = { onLogoutClicked() }
        ) {
            Text(text = stringResource(id = R.string.sign_out_button))
        }
    }
}

@PreviewLightDark
@PreviewScreenSizes
@Composable
private fun SettingsPreview() {
    HexagonalGamesTheme {
        AccountElements(
            onDeleteClick = {},
            onLogoutClicked = {},
            uiState = AccountUiState(
                currentUser = null,
                errorMessage = null
            )
        )
    }
}