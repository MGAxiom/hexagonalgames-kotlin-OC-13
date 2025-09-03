package com.openclassrooms.hexagonal.games.screen.account

import com.openclassrooms.hexagonal.games.screen.settings.SettingsViewModel

import android.os.Build
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    viewModel: AccountViewModel = hiltViewModel(),
    onBackClick: () -> Unit,
    onLoggedOut: () -> Unit,
    onAccountDeleted: () -> Unit
) {
    LaunchedEffect(key1 = viewModel) {
        viewModel.logoutEvent.collectLatest {
            onLoggedOut()
        }
    }

    LaunchedEffect(key1 = viewModel) {
        viewModel.accountDeletedEvent.collectLatest {
            onAccountDeleted()
        }
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
            onDeleteClick = { viewModel.onDeleteAccount() },
            onLogoutClicked = { viewModel.onLogout() }
        )
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun AccountElements(
    modifier: Modifier = Modifier,
    onLogoutClicked: () -> Unit,
    onDeleteClick: () -> Unit
) {

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
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
            onLogoutClicked = {}
        )
    }
}