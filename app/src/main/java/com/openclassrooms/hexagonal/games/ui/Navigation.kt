package com.openclassrooms.hexagonal.games.ui

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.openclassrooms.hexagonal.games.screen.Screen
import com.openclassrooms.hexagonal.games.screen.account.AccountScreen
import com.openclassrooms.hexagonal.games.screen.account.AccountViewModel
import com.openclassrooms.hexagonal.games.screen.ad.AddScreen
import com.openclassrooms.hexagonal.games.screen.homefeed.HomefeedScreen
import com.openclassrooms.hexagonal.games.screen.settings.SettingsScreen
import com.openclassrooms.hexagonal.games.ui.state.NavigationEvent

@Composable
fun HexagonalGamesNavHost(
    navHostController: NavHostController,
    activitySignInLauncher: () -> Unit,
) {
    NavHost(
        navController = navHostController,
        startDestination = Screen.Homefeed.route
    ) {
        composable(route = Screen.Homefeed.route) {
            HomefeedScreen(
                onPostClick = {
                    //TODO
                },
                onSettingsClick = {
                    navHostController.navigate(Screen.Settings.route)
                },
                onFABClick = {
                    navHostController.navigate(Screen.AddPost.route)
                },
                onAccountClick = {
                    navHostController.navigate(Screen.Account.route)
                }
            )
        }
        composable(route = Screen.AddPost.route) {
            AddScreen(
                onBackClick = { navHostController.navigateUp() },
                onSaveClick = { navHostController.navigateUp() }
            )
        }
        composable(route = Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navHostController.navigateUp() }
            )
        }
        composable(route = Screen.Account.route) {
            val accountViewModel: AccountViewModel =
                androidx.hilt.navigation.compose.hiltViewModel()
            val uiState by accountViewModel.uiState.collectAsState()

            LaunchedEffect(uiState.navigationEvent) {
                when (val event = uiState.navigationEvent) {
                    is NavigationEvent.RequestActivitySignIn -> {
                        activitySignInLauncher()
                        accountViewModel.consumeNavigationEvent()
                    }

                    is NavigationEvent.LogoutCompleted -> {
                        navHostController.popBackStack(
                            Screen.Homefeed.route,
                            inclusive = false,
                            saveState = false
                        )
                        accountViewModel.consumeNavigationEvent()
                    }

                    is NavigationEvent.AccountDeletionCompleted -> {
                        navHostController.popBackStack(
                            Screen.Homefeed.route,
                            inclusive = false,
                            saveState = false
                        )
                        accountViewModel.consumeNavigationEvent()
                    }

                    else -> Unit
                }
            }

            AccountScreen(
                uiState = uiState,
                onConsumeError = { accountViewModel.consumeErrorMessage() },
                onBackClick = { navHostController.navigateUp() },
                onSignInRequested = {
//                    accountViewModel.onSignInRequested()
                },
                onLogoutClicked = { accountViewModel.onLogout() },
                onDeleteAccountClicked = { accountViewModel.onDeleteAccount() },
                onRefresh = { accountViewModel.refreshUserState() }
            )
        }
    }
}