package com.openclassrooms.hexagonal.games.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {

    fun getCurrentUser() = firebaseAuth.currentUser

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun deleteCurrentUserAccount(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = firebaseAuth.currentUser
        user?.delete()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    task.exception?.let { onFailure(it) }
                    Log.v("AuthRepository", "deleteCurrentUserAccount: ${task.exception}")
                }
            } ?: onFailure(IllegalStateException("User not found to delete."))
    }
}