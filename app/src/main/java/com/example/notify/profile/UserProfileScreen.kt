package com.example.notify.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import androidx.navigation.NavController

@Composable
fun UserProfileScreen(
    userId: String,
    navController: NavController
) {

    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var username by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf("") }
    var followers by remember { mutableStateOf(0L) }
    var following by remember { mutableStateOf(0L) }
    var isFollowing by remember { mutableStateOf(false) }

    // 🔥 NEW PUBLIC PROFILE FIELDS (ADDED ONLY)
    var firstName by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    // 🔥 REAL TIME USER DATA
    LaunchedEffect(userId) {

        if (userId.isEmpty()) return@LaunchedEffect

        db.collection("users")
            .document(userId)
            .addSnapshotListener { doc, _ ->
                if (doc != null && doc.exists()) {
                    username = doc.getString("username") ?: ""
                    photoUrl = doc.getString("photoUrl") ?: ""
                    followers = doc.getLong("followers") ?: 0
                    following = doc.getLong("following") ?: 0

                    // 🔥 LOAD NEW FIELDS
                    firstName = doc.getString("firstName") ?: ""
                    surname = doc.getString("surname") ?: ""
                    bio = doc.getString("bio") ?: ""
                }
            }

        if (currentUserId.isNotEmpty()) {
            db.collection("follows")
                .whereEqualTo("followerId", currentUserId)
                .whereEqualTo("followingId", userId)
                .addSnapshotListener { snapshot, _ ->
                    isFollowing = snapshot?.isEmpty == false
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0E0E13), Color(0xFF1A1A22))
                )
            )
            .padding(20.dp)
    ) {

        // 🔥 TOP BAR
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = username,
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 🔥 PROFILE IMAGE
        AsyncImage(
            model = photoUrl,
            contentDescription = null,
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🔥 REAL NAME (FIRST + SURNAME)
        if (firstName.isNotEmpty() || surname.isNotEmpty()) {
            Text(
                text = "$firstName $surname",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 🔥 BIO
        if (bio.isNotEmpty()) {
            Text(
                text = bio,
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 🔥 FOLLOWERS / FOLLOWING ROW
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = followers.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text("Followers", color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = following.toString(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text("Following", color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // 🔥 FOLLOW BUTTON
        if (currentUserId != userId && currentUserId.isNotEmpty()) {

            Button(
                onClick = {

                    db.collection("follows")
                        .whereEqualTo("followerId", currentUserId)
                        .whereEqualTo("followingId", userId)
                        .get()
                        .addOnSuccessListener { snapshot ->

                            if (snapshot.isEmpty) {

                                db.collection("follows")
                                    .add(
                                        hashMapOf(
                                            "followerId" to currentUserId,
                                            "followingId" to userId,
                                            "timestamp" to System.currentTimeMillis()
                                        )
                                    )

                                db.collection("users").document(currentUserId)
                                    .update("following", FieldValue.increment(1))

                                db.collection("users").document(userId)
                                    .update("followers", FieldValue.increment(1))

                                db.collection("notifications")
                                    .add(
                                        hashMapOf(
                                            "toUserId" to userId,
                                            "fromUserId" to currentUserId,
                                            "type" to "follow",
                                            "timestamp" to System.currentTimeMillis(),
                                            "seen" to false
                                        )
                                    )

                            } else {

                                snapshot.documents.forEach {
                                    db.collection("follows").document(it.id).delete()
                                }

                                db.collection("users").document(currentUserId)
                                    .update("following", FieldValue.increment(-1))

                                db.collection("users").document(userId)
                                    .update("followers", FieldValue.increment(-1))
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor =
                        if (isFollowing)
                            Color.Gray
                        else
                            Color(0xFF6A5AE0)
                )
            ) {
                Text(
                    text = if (isFollowing) "Following" else "Follow",
                    color = Color.White
                )
            }
        }
    }
}
