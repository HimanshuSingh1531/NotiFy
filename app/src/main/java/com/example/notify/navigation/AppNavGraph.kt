package com.example.notify.navigation

import androidx.activity.compose.BackHandler
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notify.auth.*
import com.example.notify.profile.ProfileScreen
import com.example.notify.profile.ProfileViewModel
import com.example.notify.profile.EditProfileScreen   // ✅ ADDED

@Composable
fun AppNavGraph(
    googleAuthClient: GoogleAuthClient,
    googleLauncher: ActivityResultLauncher<android.content.Intent>,
    googleLoginSuccess: MutableState<Boolean>
) {

    val authViewModel: AuthViewModel = viewModel()
    val profileViewModel: ProfileViewModel = viewModel()

    val showSignup = remember { mutableStateOf(false) }
    val showUsername = remember { mutableStateOf(false) }
    val goToProfile = remember { mutableStateOf(false) }
    val isLoading = remember { mutableStateOf(false) }

    val showEdit = remember { mutableStateOf(false) }   // ✅ ADDED

    // 🔥 Google login listener (FIXED PROPERLY)
    LaunchedEffect(googleLoginSuccess.value) {
        if (googleLoginSuccess.value) {

            isLoading.value = true

            authViewModel.createGoogleUserIfNotExists {

                authViewModel.checkIfUsernameExists { exists ->

                    isLoading.value = false

                    if (exists) {
                        goToProfile.value = true
                    } else {
                        showUsername.value = true
                    }
                }
            }

            googleLoginSuccess.value = false
        }
    }

    BackHandler(enabled = showSignup.value || showUsername.value || showEdit.value) {  // ✅ MODIFIED
        when {
            showEdit.value -> showEdit.value = false     // ✅ ADDED
            showUsername.value -> showUsername.value = false
            showSignup.value -> showSignup.value = false
        }
    }

    when {

        // ✏ EDIT PROFILE SCREEN (NEW BLOCK)
        showEdit.value -> {   // ✅ ADDED
            EditProfileScreen(
                profileViewModel = profileViewModel,
                onSave = {
                    showEdit.value = false
                }
            )
        }

        // 👤 PROFILE SCREEN
        goToProfile.value -> {
            ProfileScreen(
                profileViewModel = profileViewModel,
                onEditClick = {
                    showEdit.value = true    // ✅ ADDED (THIS FIXES PENCIL)
                }
            )
        }

        // 🆕 USERNAME SCREEN
        showUsername.value -> {
            UsernameScreen(
                authViewModel = authViewModel,
                defaultName = "notifyuser",
                onUsernameConfirmed = {
                    goToProfile.value = true
                }
            )
        }

        // 📝 SIGNUP
        showSignup.value -> {
            SignupScreen(
                authViewModel = authViewModel,
                onSignupSuccess = {
                    showSignup.value = false
                }
            )
        }

        // 🔐 LOGIN
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

                    isLoading.value = true

                    authViewModel.checkIfUsernameExists { exists ->
                        isLoading.value = false

                        if (exists) {
                            goToProfile.value = true
                        } else {
                            showUsername.value = true
                        }
                    }
                },
                isLoading = isLoading.value
            )
        }
    }
}
