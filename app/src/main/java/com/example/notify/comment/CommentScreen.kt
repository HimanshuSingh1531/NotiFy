package com.example.notify.comment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.navigation.NavController
import kotlin.math.abs

@Composable
fun CommentScreen(
    postId: String,
    navController: NavController
) {

    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var commentText by remember { mutableStateOf("") }
    var comments by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    // 🔥 REAL TIME COMMENTS
    LaunchedEffect(postId) {

        db.collection("posts")
            .document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->

                comments = snapshot?.documents?.mapNotNull {
                    it.data?.plus("commentId" to it.id)
                } ?: emptyList()
            }
    }

    fun formatTime(time: Long): String {
        val diff = System.currentTimeMillis() - time
        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            seconds < 60 -> "Just now"
            minutes < 60 -> "${minutes}m"
            hours < 24 -> "${hours}h"
            else -> "${days}d"
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
            .padding(16.dp)
    ) {

        // 🔥 TOP BAR
        Row(verticalAlignment = Alignment.CenterVertically) {

            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                "Comments (${comments.size})",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {

            items(comments) { comment ->

                val userId = comment["userId"] as? String ?: ""
                val text = comment["text"] as? String ?: ""
                val timestamp = comment["timestamp"] as? Long ?: 0
                val commentId = comment["commentId"] as? String ?: ""

                var username by remember { mutableStateOf("") }
                var profileUrl by remember { mutableStateOf("") }

                LaunchedEffect(userId) {
                    db.collection("users")
                        .document(userId)
                        .get()
                        .addOnSuccessListener {
                            username = it.getString("username") ?: ""
                            profileUrl = it.getString("photoUrl") ?: ""
                        }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {

                    AsyncImage(
                        model = profileUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable {
                                navController.navigate("profile_view/$userId")
                            }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Text(
                                username,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.clickable {
                                    navController.navigate("profile_view/$userId")
                                }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Text(
                                formatTime(timestamp),
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }

                        Text(
                            text,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }

                    // DELETE BUTTON (only own comment)
                    if (userId == currentUserId) {
                        IconButton(
                            onClick = {
                                db.collection("posts")
                                    .document(postId)
                                    .collection("comments")
                                    .document(commentId)
                                    .delete()
                            }
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = Color.Red
                            )
                        }
                    }
                }
            }
        }

        // 🔥 COMMENT INPUT BAR
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {

            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Add a comment...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6A5AE0),
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            IconButton(
                onClick = {

                    if (commentText.isEmpty()) return@IconButton

                    val comment = hashMapOf(
                        "userId" to currentUserId,
                        "text" to commentText,
                        "timestamp" to System.currentTimeMillis()
                    )

                    db.collection("posts")
                        .document(postId)
                        .collection("comments")
                        .add(comment)

                    commentText = ""
                }
            ) {
                Icon(Icons.Default.Send, null, tint = Color(0xFF6A5AE0))
            }
        }
    }
}
