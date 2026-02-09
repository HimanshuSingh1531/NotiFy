package com.example.notify.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UsernameScreen(
    authViewModel: AuthViewModel,          // 🔥 ADDED
    defaultName: String,
    onUsernameConfirmed: (String) -> Unit
) {

    var username by remember { mutableStateOf("@$defaultName") }
    var error by remember { mutableStateOf("") }

    val fieldShape = RoundedCornerShape(14.dp)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0E0E13), Color(0xFF1A1A22))
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
                .background(Color(0xFF1F1F27), RoundedCornerShape(22.dp))
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF7B61FF), Color(0xFF4DA3FF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "N",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "Choose your username",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(6.dp))

            Text(
                "This will be your unique identity",
                color = Color.Gray,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    error = ""
                },
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape,
                placeholder = { Text("@username") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White,
                    focusedBorderColor = Color(0xFF6A5AE0),
                    unfocusedBorderColor = Color.DarkGray
                )
            )

            if (error.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(error, color = Color.Red, fontSize = 12.sp)
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    if (!username.startsWith("@")) {
                        error = "Username must start with @"
                    } else if (username.length < 5) {
                        error = "Username is too short"
                    } else {
                        // 🔥 FIRESTORE UNIQUE CHECK (ADDED)
                        authViewModel.isUsernameAvailable(username) { available ->
                            if (available) {
                                authViewModel.saveUsername(
                                    username = username,
                                    onSuccess = {
                                        onUsernameConfirmed(username)
                                    },
                                    onError = { error = it }
                                )
                            } else {
                                error = "Username already taken"
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = fieldShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6A5AE0)
                )
            ) {
                Text("Continue")
            }
        }
    }
}
