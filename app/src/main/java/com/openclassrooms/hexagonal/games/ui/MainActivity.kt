package com.openclassrooms.hexagonal.games.ui

import android.os.Bundle
import android.util.Log
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
import com.google.firebase.analytics.FirebaseAnalytics
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
    private var isMainContentSet: Boolean by mutableStateOf(false)
    private val TAG = "MainActivityAuth"

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
        val authUser = firebaseAuth.currentUser
        Log.d(TAG, "AuthStateListener: User: ${authUser?.uid}, MainContentSet: $isMainContentSet")
        if (this.currentUser != authUser) this.currentUser = authUser

        if (authUser != null) {
            if (!isMainContentSet) setupMainContent()
        } else {
            if (isMainContentSet) isMainContentSet = false
            if (!isFinishing) signInLauncher.launch(signInIntent)
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            Log.d(TAG, "Sign-in successful. User: ${FirebaseAuth.getInstance().currentUser?.uid}")
            this.currentUser = FirebaseAuth.getInstance().currentUser
        } else {
            val response = result.idpResponse
            if (response == null) {
                Log.d(TAG, "Sign-in cancelled by user.")
                if (FirebaseAuth.getInstance().currentUser == null && !isFinishing && !isMainContentSet) {
                    Log.d(
                        TAG,
                        "User cancelled, not logged in, content not set. Relying on AuthStateListener."
                    )
                }
                return
            }
        }
    }

    override fun onStart() {
        super.onStart()
        FirebaseAuth.getInstance().addAuthStateListener(authStateListener)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        analytics = FirebaseAnalytics.getInstance(this)
    }

    private fun setupMainContent() {
        setContent {
            val navController = rememberNavController()
            HexagonalGamesTheme {
                HexagonalGamesNavHost(
                    navHostController = navController,
                    activitySignInLauncher = {
                        if (FirebaseAuth.getInstance().currentUser == null) {
                            signInLauncher.launch(signInIntent)
                        }
                    }
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        FirebaseAuth.getInstance().removeAuthStateListener(authStateListener)
    }
}


