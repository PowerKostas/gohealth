package com.healthterra.services

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

// Standard code that creates and handles the Google sign-in popup
suspend fun linkWithGoogleSignIn(context: Context): Boolean {
    val credentialManager = CredentialManager.create(context)
    val webClientId = "487763726399-6t1rcsmvoja8imbhvjufe89m1ikvgi6h.apps.googleusercontent.com"

    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(webClientId)
        .setAutoSelectEnabled(true)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    return try {
        val result = credentialManager.getCredential(context = context, request = request)
        val credential = result.credential

        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)

            // Links the Anonymous account to an existing or new Google account
            val currentUser = Firebase.auth.currentUser
            if (currentUser != null) {
                try {
                    currentUser.linkWithCredential(firebaseCredential).await()
                    currentUser.reload().await()
                    Log.d("Google sign-in", "Successfully linked Google account")
                    true
                }

                catch (e: FirebaseAuthUserCollisionException) {
                    Log.d("Google sign-in", "Account collision: Signing in instead of linking", e)
                    Firebase.auth.signInWithCredential(firebaseCredential).await()
                    true
                }
            }

            else {
                Log.e("Google sign-in", "Session expired")
                false
            }
        }

        else {
            Log.e("Google sign-in", "Unexpected credential type")
            false
        }
    }

    catch (e: Exception) {
        Log.e("Auth", "Google sign-in failed", e)
        false
    }
}
