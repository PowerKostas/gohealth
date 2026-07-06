package com.healthterra.services

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

// Standard code that creates and handles the Google sign-in popup
// Return type: 0 = Fail, 1 = Fail and no error message, 2 = Success, 3 = Success and merge message
suspend fun linkWithGoogleSignIn(context: Context): Int {
    val credentialManager = CredentialManager.create(context)
    val webClientId = "487763726399-6t1rcsmvoja8imbhvjufe89m1ikvgi6h.apps.googleusercontent.com"

    val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(webClientId).build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(signInWithGoogleOption)
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
                    2
                }

                catch (e: FirebaseAuthUserCollisionException) {
                    Log.d("Google sign-in", "Account collision: Signing in instead of linking", e)
                    Firebase.auth.signInWithCredential(firebaseCredential).await()
                    3
                }
            }

            // To allow the user to sign back in, if they sign out and a UID hasn't been assigned yet. Race conditions can happen causing the
            // user to sign out of their Google account when the anonymous auth happens, but it's better than not being able to sign in
            // immediately after signing out
            else {
                Log.d("Google sign-in", "No anonymous user found: Signing in instead of linking")
                Firebase.auth.signInWithCredential(firebaseCredential).await()
                3
            }
        }

        else {
            Log.e("Google sign-in", "Unexpected credential type")
            0
        }
    }

    catch (e: GetCredentialCancellationException) {
        Log.d("Google sign-in", "User cancelled the sign-in flow", e)
        1
    }

    catch (e: Exception) {
        Log.e("Google sign-in", "Google sign-in failed", e)
        0
    }
}
