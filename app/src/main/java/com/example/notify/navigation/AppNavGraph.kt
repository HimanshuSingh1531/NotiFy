package com.example.notify.navigation

import androidx.activity.compose.BackHandler
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notify.auth.*

@Composable
fun AppNavGraph(
    googleAuthClient: GoogleAuthClient,
    googleLauncher: ActivityResultLauncher<android.content.Intent>,
    googleLoginSuccess: MutableState<Boolean>
) {

    val authViewModel: AuthViewModel = viewModel()

    val showSignup = remember { mutableStateOf(false) }
    val showUsername = remember { mutableStateOf(false) }

    // 🔥 LISTEN FOR GOOGLE LOGIN SUCCESS
    LaunchedEffect(googleLoginSuccess.value) {
        if (googleLoginSuccess.value) {
            showUsername.value = true
            googleLoginSuccess.value = false
        }
    }

    // 🔙 BACK handling
    BackHandler(enabled = showSignup.value || showUsername.value) {
        when {
            showUsername.value -> showUsername.value = false
            showSignup.value -> showSignup.value = false
        }
    }

    when {
        // 🆕 USERNAME SCREEN
        showUsername.value -> {
            UsernameScreen(
                authViewModel = authViewModel,     // ✅ FIX
                defaultName = "notifyuser",
                onUsernameConfirmed = { username ->
                    // TODO: Navigate to Home
                }
            )
        }

        // 📝 SIGNUP SCREEN
        showSignup.value -> {
            SignupScreen(
                authViewModel = authViewModel,
                onSignupSuccess = {
                    showSignup.value = false
                }
            )
        }

        // 🔐 LOGIN SCREEN
        else -> {
            LoginScreen(
                authViewModel = authViewModel,

                onGoogleClick = {
                    googleAuthClient.signOut()
                    googleLauncher.launch(
                        googleAuthClient.getSignInIntent()
                    )
                },

                onSignupClick = {
                    showSignup.value = true
                },

                onLoginSuccess = {
                    // ✅ EMAIL/PASSWORD LOGIN
                    showUsername.value = true
                }
            )
        }
    }
}
