package com.example.notify.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleAuthClient(context: Context) {

    private val auth = FirebaseAuth.getInstance()

    private val googleSignInClient = GoogleSignIn.getClient(
        context,
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(
                "199732529907-l0uaessempm5ca0gakj9v896i769c5af.apps.googleusercontent.com"
            )
            .requestEmail()
            .build()
    )

    // 🔹 Google Sign-In Intent
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    // 🔹 FORCE ACCOUNT PICKER (IMPORTANT)
    fun signOut() {
        googleSignInClient.signOut()
    }

    // 🔹 Firebase Sign-In with Google
    suspend fun signInWithIntent(intent: Intent) {
        val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
        val account = task.getResult(Exception::class.java)

        val credential = GoogleAuthProvider.getCredential(
            account.idToken,
            null
        )

        auth.signInWithCredential(credential).await()
    }

    fun getCurrentUser() = auth.currentUser
}
