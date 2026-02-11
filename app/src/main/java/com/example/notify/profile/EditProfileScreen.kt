package com.example.notify.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EditProfileScreen(
    profileViewModel: ProfileViewModel,
    onSave: () -> Unit
) {

    var firstName by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    val fieldShape = RoundedCornerShape(14.dp)

    // 🔥 Load existing data
    LaunchedEffect(Unit) {
        profileViewModel.loadUserData {
            firstName = it["firstName"] as? String ?: ""
            surname = it["surname"] as? String ?: ""
            username = it["username"] as? String ?: ""
            phone = it["phone"] as? String ?: ""
            bio = it["bio"] as? String ?: ""
        }
    }

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
                .fillMaxWidth()
                .padding(20.dp)
        ) {

            Text(
                "Edit Profile",
                color = Color.White,
                fontSize = 22.sp
            )

            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                placeholder = { Text("First Name") },
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape,
                colors = thinWhiteFieldColors()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = surname,
                onValueChange = { surname = it },
                placeholder = { Text("Surname") },
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape,
                colors = thinWhiteFieldColors()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { Text("Username") },
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape,
                colors = thinWhiteFieldColors()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = { Text("Phone Number") },
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape,
                colors = thinWhiteFieldColors()
            )

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                placeholder = { Text("Bio") },
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape,
                colors = thinWhiteFieldColors()
            )

            if (error.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                Text(error, color = Color.Red)
            }

            Spacer(Modifier.height(30.dp))

            Button(
                onClick = {

                    // ✅ MUST START WITH @
                    if (!username.startsWith("@")) {
                        error = "Username must start with @"
                        return@Button
                    }

                    // 🔥 NEW: CHECK IF USERNAME ALREADY EXISTS
                    profileViewModel.checkUsernameAvailability(
                        username = username,
                        onResult = { available ->

                            if (!available) {
                                error = "Username already exists"
                                return@checkUsernameAvailability
                            }

                            // ✅ If available → update
                            profileViewModel.updateProfile(
                                firstName = firstName,
                                surname = surname,
                                bio = bio,
                                phone = phone,
                                username = username,
                                onSuccess = { onSave() },
                                onError = { error = it }
                            )
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = fieldShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6A5AE0)
                )
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
private fun thinWhiteFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color.White.copy(alpha = 0.6f),
    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color.White
)
