package com.example.notify.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream
import com.yalantis.ucrop.UCrop

@Composable
fun EditProfileScreen(
    profileViewModel: ProfileViewModel,
    onSave: () -> Unit
) {

    val context = LocalContext.current

    var firstName by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingPhotoUrl by remember { mutableStateOf("") }

    // 🔥 CROP RESULT LAUNCHER
    val cropLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->

        if (result.resultCode == Activity.RESULT_OK) {
            val resultUri = UCrop.getOutput(result.data!!)
            selectedImageUri = resultUri
        }
    }

    // 🔥 IMAGE PICKER (NOW STARTS UCROP)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->

        uri?.let {

            val destinationUri = Uri.fromFile(
                File(context.cacheDir, "cropped_image.jpg")
            )

            val uCrop = UCrop.of(it, destinationUri)
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(600, 600)

            cropLauncher.launch(uCrop.getIntent(context))
        }
    }

    val fieldShape = RoundedCornerShape(14.dp)

    LaunchedEffect(Unit) {
        profileViewModel.loadUserData {
            firstName = it["firstName"] as? String ?: ""
            surname = it["surname"] as? String ?: ""
            username = it["username"] as? String ?: ""
            phone = it["phone"] as? String ?: ""
            bio = it["bio"] as? String ?: ""
            existingPhotoUrl = it["photoUrl"] as? String ?: ""
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

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
                    .clickable {
                        imagePickerLauncher.launch("image/*")
                    },
                contentAlignment = Alignment.Center
            ) {

                when {
                    selectedImageUri != null -> {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    existingPhotoUrl.isNotEmpty() -> {
                        AsyncImage(
                            model = existingPhotoUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    else -> {
                        Text(
                            firstName.firstOrNull()?.toString() ?: "N",
                            color = Color.White,
                            fontSize = 30.sp
                        )
                    }
                }
            }

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

                    if (!username.startsWith("@")) {
                        error = "Username must start with @"
                        return@Button
                    }

                    profileViewModel.checkUsernameAvailability(
                        username = username
                    ) { available ->

                        if (!available) {
                            error = "Username already exists"
                            return@checkUsernameAvailability
                        }

                        profileViewModel.updateProfile(
                            firstName = firstName,
                            surname = surname,
                            bio = bio,
                            phone = phone,
                            username = username,
                            onSuccess = {

                                selectedImageUri?.let { uri ->

                                    val file = uriToFile(context, uri)

                                    profileViewModel.uploadProfileImage(
                                        file = file,
                                        onSuccess = { onSave() },
                                        onError = { errorMsg ->
                                            error = errorMsg
                                        }
                                    )

                                } ?: onSave()
                            },
                            onError = { error = it }
                        )
                    }
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

private fun uriToFile(context: Context, uri: Uri): File {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "upload_image.jpg")
    val outputStream = FileOutputStream(file)
    inputStream?.copyTo(outputStream)
    inputStream?.close()
    outputStream.close()
    return file
}

@Composable
private fun thinWhiteFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color.White.copy(alpha = 0.6f),
    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    cursorColor = Color.White
)
