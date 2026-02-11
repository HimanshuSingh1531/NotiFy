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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SignupScreen(
    authViewModel: AuthViewModel,
    onSignupSuccess: () -> Unit
) {

    var firstName by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+91") }

    var passwordVisible by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

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
                .padding(22.dp)
        ) {

            Text("Create Account", fontSize = 22.sp, color = Color.White)

            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                placeholder = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = surname,
                onValueChange = { surname = it },
                placeholder = { Text("Surname") },
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape,
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
                }
            )

            Spacer(Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {

                OutlinedTextField(
                    value = countryCode,
                    onValueChange = { countryCode = it },
                    modifier = Modifier.width(80.dp),
                    shape = fieldShape,
                    singleLine = true
                )

                Spacer(Modifier.width(8.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = { Text("Phone Number") },
                    modifier = Modifier.weight(1f),
                    shape = fieldShape,
                    singleLine = true
                )
            }

            Spacer(Modifier.height(14.dp))

            if (successMsg.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color(0xFFEDEAFF),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(10.dp)
                ) {
                    Text(
                        text = successMsg,
                        color = Color(0xFF4B3EFF),
                        fontSize = 13.sp
                    )
                }

                Spacer(Modifier.height(10.dp))
            }

            if (error.isNotEmpty()) {
                Text(error, color = Color.Red, fontSize = 12.sp)
                Spacer(Modifier.height(6.dp))
            }

            Button(
                onClick = {
                    error = ""
                    successMsg = ""
                    isLoading = true

                    authViewModel.signup(
                        firstName,
                        surname,
                        email,
                        password,
                        "$countryCode$phone",
                        onSuccess = {
                            isLoading = false
                            successMsg = "Signup successfully! Please login."
                            onSignupSuccess()
                        },
                        onError = {
                            isLoading = false
                            error = it
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = fieldShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6A5AE0)
                )
            ) {
                Text("Sign Up")
            }
        }

        // 🔄 LOADING OVERLAY (VISIBLE THIN WHITE RING + N LOGO)
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Box(contentAlignment = Alignment.Center) {

                        CircularProgressIndicator(
                            color = Color.White.copy(alpha = 0.85f),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(64.dp)
                        )

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
                                fontSize = 30.sp,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        "Please wait...",
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}
