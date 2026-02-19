package com.example.notify.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.clickable // 🔥 ADDED
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert // 🔥 ADDED
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController // 🔥 ADDED
import com.google.firebase.auth.FirebaseAuth

import coil.compose.AsyncImage

@Composable
fun ProfileScreen(
    profileViewModel: ProfileViewModel,
    onEditClick: () -> Unit,
    onLogout: () -> Unit, // 🔥 ADDED
    navController: NavController // 🔥 ADDED
) {

    var userData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var bioState by remember { mutableStateOf("") }
    var phoneState by remember { mutableStateOf("") }

    var showMenu by remember { mutableStateOf(false) } // 🔥 ADDED

    LaunchedEffect(Unit) {
        profileViewModel.loadUserData {
            userData = it
            bioState = it["bio"] as? String ?: ""
            phoneState = it["phone"] as? String ?: ""
        }
    }

    if (userData == null) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val firstName = userData?.get("firstName") as? String ?: ""
    val surname = userData?.get("surname") as? String ?: ""
    val username = userData?.get("username") as? String ?: ""
    val followers = userData?.get("followers") as? Long ?: 0
    val following = userData?.get("following") as? Long ?: 0

    val photoUrl = userData?.get("photoUrl") as? String ?: ""
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var tempBio by remember { mutableStateOf("") }
    var tempPhone by remember { mutableStateOf("") }

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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Profile",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, null, tint = Color.White)
                    }

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Default.MoreVert, null, tint = Color.White)
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        "Logout",
                                        color = Color.White
                                    )
                                },
                                onClick = {
                                    showMenu = false
                                    profileViewModel.logout {
                                        onLogout()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF7B61FF), Color(0xFF4DA3FF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {

                if (photoUrl.isNotEmpty()) {

                    AsyncImage(
                        model = photoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                    )

                } else {

                    Text(
                        firstName.firstOrNull()?.toString() ?: "N",
                        fontSize = 36.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text(
                "$firstName $surname",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                username,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(Modifier.height(16.dp))

            if (phoneState.isEmpty()) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    OutlinedTextField(
                        value = tempPhone,
                        onValueChange = { tempPhone = it },
                        placeholder = { Text("Add phone number") },
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White.copy(alpha = 0.6f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White
                        )
                    )

                    IconButton(
                        onClick = {
                            profileViewModel.updateProfile(
                                firstName = firstName,
                                surname = surname,
                                bio = bioState,
                                phone = tempPhone,
                                username = username,
                                onSuccess = {
                                    phoneState = tempPhone
                                },
                                onError = {}
                            )
                        }
                    ) {
                        Icon(Icons.Default.Check, null, tint = Color.White)
                    }
                }

            } else {

                Text(
                    phoneState,
                    fontSize = 14.sp,
                    color = Color.LightGray
                )
            }

            Spacer(Modifier.height(12.dp))

            if (bioState.isEmpty()) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    OutlinedTextField(
                        value = tempBio,
                        onValueChange = { tempBio = it },
                        placeholder = { Text("Add bio") },
                        shape = RoundedCornerShape(20.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.White.copy(alpha = 0.6f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White
                        )
                    )

                    IconButton(
                        onClick = {
                            profileViewModel.updateProfile(
                                firstName = firstName,
                                surname = surname,
                                bio = tempBio,
                                phone = phoneState,
                                username = username,
                                onSuccess = {
                                    bioState = tempBio
                                },
                                onError = {}
                            )
                        }
                    ) {
                        Icon(Icons.Default.Check, null, tint = Color.White)
                    }
                }

            } else {

                Text(
                    bioState,
                    fontSize = 14.sp,
                    color = Color.LightGray
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                // 🔥 FOLLOWERS CLICKABLE (ONLY ADDED)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        navController.navigate("follow_list/$currentUserId/followers")
                    }
                ) {
                    Text("$followers", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Followers", color = Color.Gray)
                }

                // 🔥 FOLLOWING CLICKABLE (ONLY ADDED)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        navController.navigate("follow_list/$currentUserId/following")
                    }
                ) {
                    Text("$following", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Following", color = Color.Gray)
                }
            }

            Spacer(Modifier.height(24.dp))

            Text("My Posts", color = Color.White, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(10.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize()
            ) {
                items(0) { }
            }
        }
    }
}

@Composable
fun StatItem(count: Long, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$count", color = Color.White, fontWeight = FontWeight.Bold)
        Text(label, color = Color.Gray)
    }
}
