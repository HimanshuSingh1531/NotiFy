package com.example.notify

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.lifecycleScope
import com.example.notify.auth.GoogleAuthClient
import com.example.notify.navigation.AppNavGraph
import com.example.notify.ui.theme.NotiFyTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var googleAuthClient: GoogleAuthClient
    private lateinit var googleLauncher: ActivityResultLauncher<android.content.Intent>

    // 🔥 SHARED STATE (THIS IS THE FIX)
    private val googleLoginSuccess = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        googleAuthClient = GoogleAuthClient(this)

        googleLauncher =
            registerForActivityResult(
                ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == RESULT_OK && result.data != null) {
                    lifecycleScope.launch {
                        try {
                            googleAuthClient.signInWithIntent(result.data!!)
                            // ✅ GOOGLE LOGIN SUCCESS
                            googleLoginSuccess.value = true
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

        setContent {
            NotiFyTheme {
                AppNavGraph(
                    googleAuthClient = googleAuthClient,
                    googleLauncher = googleLauncher,
                    googleLoginSuccess = googleLoginSuccess
                )
            }
        }
    }
}
