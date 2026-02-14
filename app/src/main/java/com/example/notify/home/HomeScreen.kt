package com.example.notify.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore

// 🔥 NEW IMPORTS ADDED (NOT REMOVING ANYTHING)
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue

// 🔥🔥🔥 NEW IMPORT ADDED FOR NAVIGATION (NO LINE REMOVED)
import androidx.navigation.NavController

@Composable
fun HomeScreen(
    navController: NavController   // 🔥 ADDED PARAMETER
) {

    var searchText by remember { mutableStateOf("") }
    var allUsers by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var filteredUsers by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    val db = FirebaseFirestore.getInstance()

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var followingList by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {

        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                allUsers = result.documents.mapNotNull { doc ->
                    doc.data?.plus("uid" to doc.id)
                }
            }

        if (currentUserId.isNotEmpty()) {
            db.collection("follows")
                .whereEqualTo("followerId", currentUserId)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        followingList = snapshot.documents.mapNotNull {
                            it.getString("followingId")
                        }
                    }
                }
        }
    }

    fun filterUsers(query: String) {

        val cleanQuery = query.removePrefix("@").trim().lowercase()

        if (cleanQuery.isEmpty()) {
            filteredUsers = emptyList()
            return
        }

        filteredUsers = allUsers.filter { user ->
            val username = (user["username"] as? String ?: "").lowercase()
            username.contains(cleanQuery)
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

        Text(
            "Notify",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    filterUsers(it)
                },
                placeholder = { Text("Search users...") },
                singleLine = true,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6A5AE0),
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = Color(0xFF6A5AE0),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = { filterUsers(searchText) }
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF6A5AE0)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn {

            items(filteredUsers) { user ->

                val username = user["username"] as? String ?: ""
                val photoUrl = user["photoUrl"] as? String ?: ""
                val followedUserId = user["uid"] as? String ?: ""

                val isFollowing = followingList.contains(followedUserId)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {   // 🔥 CLICK TO OPEN PROFILE
                            if (followedUserId.isNotEmpty()) {
                                navController.navigate("profile_view/$followedUserId")
                            }
                        }
                    ) {

                        AsyncImage(
                            model = photoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(45.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = username,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    Button(
                        onClick = {

                            if (currentUserId.isEmpty()) return@Button
                            if (followedUserId.isEmpty()) return@Button
                            if (currentUserId == followedUserId) return@Button

                            db.collection("follows")
                                .whereEqualTo("followerId", currentUserId)
                                .whereEqualTo("followingId", followedUserId)
                                .get()
                                .addOnSuccessListener { snapshot ->

                                    if (snapshot.isEmpty) {

                                        db.collection("follows")
                                            .add(
                                                hashMapOf(
                                                    "followerId" to currentUserId,
                                                    "followingId" to followedUserId,
                                                    "timestamp" to System.currentTimeMillis()
                                                )
                                            )

                                        db.collection("users").document(currentUserId)
                                            .update("following", FieldValue.increment(1))

                                        db.collection("users").document(followedUserId)
                                            .update("followers", FieldValue.increment(1))

                                        db.collection("notifications")
                                            .add(
                                                hashMapOf(
                                                    "toUserId" to followedUserId,
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

                                        db.collection("users").document(followedUserId)
                                            .update("followers", FieldValue.increment(-1))
                                    }
                                }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                                if (isFollowing)
                                    Color.Gray
                                else
                                    Color(0xFF6A5AE0)
                        )
                    ) {
                        Text(
                            if (isFollowing) "Following"
                            else "Follow"
                        )
                    }
                }
            }
        }
    }
}
