package com.openclassrooms.hexagonal.games.ui

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.openclassrooms.hexagonal.games.screen.Screen
import com.openclassrooms.hexagonal.games.screen.account.AccountScreen
import com.openclassrooms.hexagonal.games.screen.account.AccountViewModel
import com.openclassrooms.hexagonal.games.screen.ad.AddScreen
import com.openclassrooms.hexagonal.games.screen.ad.AddViewModel
import com.openclassrooms.hexagonal.games.screen.ad.FormEvent
import com.openclassrooms.hexagonal.games.screen.details.AddCommentsScreen
import com.openclassrooms.hexagonal.games.screen.details.PublicationDetailsScreen
import com.openclassrooms.hexagonal.games.screen.homefeed.HomefeedScreen
import com.openclassrooms.hexagonal.games.screen.settings.SettingsScreen
import com.openclassrooms.hexagonal.games.ui.state.AddNavigationEvent
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
                onPostClick = { postId ->
                    navHostController.navigate(Screen.Details.route + "/${postId}")
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
            val addViewModel: AddViewModel = androidx.hilt.navigation.compose.hiltViewModel()
            val uiState by addViewModel.uiState.collectAsState()
            val post by addViewModel.post.collectAsState()

            val imagePickerLauncherForAddScreen =
                rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? ->
                    addViewModel.onAction(FormEvent.PhotoUriChanged(uri))
                }

            LaunchedEffect(uiState.navigationEvent) {
                when (val event = uiState.navigationEvent) {
                    is AddNavigationEvent.RequestImagePicker -> {
                        imagePickerLauncherForAddScreen.launch("image/*")
                        addViewModel.consumeNavigationEvent()
                    }

                    null -> Unit
                }
            }

            AddScreen(
                post = post,
                uiState = uiState,
                onFormEvent = { event -> addViewModel.onAction(event) },
                onOpenImagePicker = { addViewModel.onOpenImagePickerRequested() },
                onSaveClick = {
                    addViewModel.addPost()
                    navHostController.navigateUp()
                },
                onBackClick = { navHostController.navigateUp() },
                onClearSelectedImage = { addViewModel.clearSelectedImage() },
                onConsumeErrorMessage = { addViewModel.consumeErrorMessage() }
            )
        }
        composable(
            route = Screen.Details.route + "/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) { backStackEntry ->
            PublicationDetailsScreen(
                onBackClick = { navHostController.navigateUp() },
                onAddComment = { postId ->
                    navHostController.navigate(Screen.Comments.route + "/${postId}")
                }
            )
        }

        composable(
            route = Screen.Comments.route + "/{postId}",
            arguments = listOf(navArgument("postId") { type = NavType.StringType })
        ) {
            AddCommentsScreen(
                onBackClick = { navHostController.navigateUp() },
                onSavedComment = { navHostController.navigateUp() }
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
                onSignInRequested = { accountViewModel.onSignInRequested() },
                onLogoutClicked = { accountViewModel.onLogout() },
                onDeleteAccountClicked = { accountViewModel.onDeleteAccount() },
                onRefresh = { accountViewModel.refreshUserState() }
            )
        }
    }
}