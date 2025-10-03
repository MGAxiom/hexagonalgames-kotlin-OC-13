package com.openclassrooms.hexagonal.games.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

fun getCurrentUserName(): Pair<String?, String?> {
    val firebaseUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
    var firstName: String? = null
    var lastName: String? = null

    firebaseUser?.displayName?.let { fullName ->
        if (fullName.isNotBlank()) {
            val parts = fullName.split(" ", limit = 2)
            firstName = parts.getOrNull(0)
            if (parts.size > 1) {
                lastName = parts.getOrNull(1)
            } else {
                Log.w(
                    "UserInfo",
                    "Display name '$fullName' might not contain a separate last name."
                )
            }
        }
    }
    return Pair(firstName, lastName)
}