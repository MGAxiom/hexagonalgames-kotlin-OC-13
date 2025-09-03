package com.openclassrooms.hexagonal.games.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.openclassrooms.hexagonal.games.R
import com.openclassrooms.hexagonal.games.screen.Screen
import com.openclassrooms.hexagonal.games.screen.account.AccountScreen
import com.openclassrooms.hexagonal.games.screen.ad.AddScreen
import com.openclassrooms.hexagonal.games.screen.homefeed.HomefeedScreen
import com.openclassrooms.hexagonal.games.screen.settings.SettingsScreen
import com.openclassrooms.hexagonal.games.ui.theme.HexagonalGamesTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the application. This activity serves as the entry point and container for the navigation
 * fragment. It handles setting up the toolbar, navigation controller, and action bar behavior.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var analytics: FirebaseAnalytics
    private var currentUser: FirebaseUser? by mutableStateOf(FirebaseAuth.getInstance().currentUser)

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            this.currentUser = user

            if (user != null) {
                setupMainContent()
            }
        } else {

            val response = result.idpResponse
            if (response == null) {

                if (FirebaseAuth.getInstance().currentUser == null) {
                    signInLauncher.launch(signInIntent)
                }
            } else {
                // Handle other errors
                // response.getError().getErrorCode()
            }
        }
    }

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        this.onSignInResult(result)
    }

    val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
    )

    val signInIntent by lazy {
        AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setCredentialManagerEnabled(true)
            .setAvailableProviders(providers)
            .setTheme(R.style.AppAuthUI)
            .build()
    }

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val newAuthUser = firebaseAuth.currentUser
        if (this.currentUser != newAuthUser) {
            this.currentUser = newAuthUser
            if (newAuthUser != null) {
                setupMainContent()
            } else {
                if (contentResolver != null && !isFinishing) {
                    signInLauncher.launch(signInIntent)
                }
            }
        } else if (newAuthUser == null && !isFinishing) {
            signInLauncher.launch(signInIntent)
        }
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
        if (FirebaseAuth.getInstance().currentUser == null) {
            if (!isFinishing) {
                signInLauncher.launch(signInIntent)
            }
        } else {
            setupMainContent()
        }
    }

    private fun setupMainContent() {
        setContent {
            val navController = rememberNavController()
            HexagonalGamesTheme {
                HexagonalGamesNavHost(navHostController = navController)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
    }
}

@Composable
fun HexagonalGamesNavHost(navHostController: NavHostController) {
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
            AccountScreen(
                onBackClick = { navHostController.navigateUp() },
                onLoggedOut = { navHostController.popBackStack(Screen.Homefeed.route, inclusive = false, saveState = false) },
                onAccountDeleted = {
                    navHostController.popBackStack(Screen.Homefeed.route, inclusive = false, saveState = false)
                }
            )
        }
    }
}


