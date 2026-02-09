package com.example.notify.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onGoogleClick: () -> Unit,
    onSignupClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) } // 👁️ ADDED

    val fieldShape = RoundedCornerShape(14.dp) // 🔥 SAME CURVE STYLE

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0E0E13), Color(0xFF1A1A22))
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(60.dp))

            Box(
                modifier = Modifier
                    .size(72.dp)
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
                    fontSize = 32.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))
            Text("NotiFy", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(6.dp))
            Text("Connect, Share & Celebrate", fontSize = 14.sp, color = Color.Gray)

            Spacer(Modifier.height(40.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F1F27), RoundedCornerShape(22.dp))
                    .padding(22.dp)
            ) {

                Text("Welcome", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Sign in to your account", color = Color.Gray)

                Spacer(Modifier.height(20.dp))

                // 📧 EMAIL (CURVED)
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = Color(0xFF6A5AE0),
                        unfocusedBorderColor = Color.DarkGray
                    )
                )

                Spacer(Modifier.height(12.dp))

                // 🔒 PASSWORD (CURVED + 👁️)
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation =
                        if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector =
                                    if (passwordVisible) Icons.Filled.Visibility
                                    else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = Color.LightGray
                            )
                        }
                    },
                    shape = fieldShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color.White,
                        focusedBorderColor = Color(0xFF6A5AE0),
                        unfocusedBorderColor = Color.DarkGray
                    )
                )

                Spacer(Modifier.height(16.dp))

                // 🔘 LOGIN BUTTON (already curved)
                Button(
                    onClick = {
                        authViewModel.login(email, password, onLoginSuccess) {
                            error = it
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
                    Text("Login")
                }

                if (error.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = Color.Red, fontSize = 12.sp)
                }

                Spacer(Modifier.height(12.dp))

                // 🆕 SIGNUP (CURVED PILL STYLE)
                OutlinedButton(
                    onClick = onSignupClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(42.dp),
                    shape = fieldShape,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Don’t have an account? Sign up")
                }

                Spacer(Modifier.height(16.dp))

                Divider(color = Color.DarkGray)

                Spacer(Modifier.height(16.dp))

                // 🔘 GOOGLE (already perfect)
                OutlinedButton(
                    onClick = onGoogleClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = fieldShape
                ) {
                    Text("Continue with Google")
                }
            }
        }
    }
}
