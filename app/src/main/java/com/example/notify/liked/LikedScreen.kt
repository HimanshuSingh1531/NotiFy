package com.example.notify.liked

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
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
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import androidx.navigation.NavController

fun getTimeAgo(timestamp: Long): String {

    val diff = System.currentTimeMillis() - timestamp

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Just now"
        minutes < 60 -> "${minutes}m ago"
        hours < 24 -> "${hours}h ago"
        else -> "${days}d ago"
    }
}

@Composable
fun LikedScreen(
    navController: NavController
) {

    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var notifications by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var followingList by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {

        if (currentUserId.isEmpty()) return@LaunchedEffect

        db.collection("notifications")
            .whereEqualTo("toUserId", currentUserId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    notifications = snapshot.documents.mapNotNull { doc ->
                        doc.data?.plus("id" to doc.id)
                    }
                }
            }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0E0E13), Color(0xFF1A1A22))
                )
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {

        Text(
            text = "Notifications",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn {

            items(notifications) { notification ->

                val fromUserId = notification["fromUserId"] as? String ?: ""
                val notificationId = notification["id"] as? String ?: ""
                val seen = notification["seen"] as? Boolean ?: false
                val type = notification["type"] as? String ?: "follow"
                val timestamp = notification["timestamp"] as? Long ?: 0L

                var username by remember { mutableStateOf("") }
                var photoUrl by remember { mutableStateOf("") }

                val isFollowing = followingList.contains(fromUserId)

                LaunchedEffect(fromUserId) {
                    db.collection("users")
                        .document(fromUserId)
                        .get()
                        .addOnSuccessListener { doc ->
                            username = doc.getString("username") ?: ""
                            photoUrl = doc.getString("photoUrl") ?: ""
                        }
                }

                LaunchedEffect(notificationId) {
                    if (!seen) {
                        db.collection("notifications")
                            .document(notificationId)
                            .update("seen", true)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable {
                            if (fromUserId.isNotEmpty()) {
                                navController.navigate("profile_view/$fromUserId")
                            }
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    if (!seen) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    AsyncImage(
                        model = photoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {

                        Text(
                            text =
                                when (type) {
                                    "follow_back" -> "$username followed you back"
                                    "unfollow" -> "$username unfollowed you"
                                    else -> "$username followed you"
                                },
                            color = Color.White,
                            fontSize = 14.sp
                        )

                        Text(
                            text = getTimeAgo(timestamp),
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }

                    Button(
                        onClick = {

                            if (fromUserId == currentUserId) return@Button

                            db.collection("follows")
                                .whereEqualTo("followerId", currentUserId)
                                .whereEqualTo("followingId", fromUserId)
                                .get()
                                .addOnSuccessListener { snapshot ->

                                    if (snapshot.isEmpty) {

                                        db.collection("follows")
                                            .add(
                                                hashMapOf(
                                                    "followerId" to currentUserId,
                                                    "followingId" to fromUserId,
                                                    "timestamp" to System.currentTimeMillis()
                                                )
                                            )

                                        db.collection("users").document(currentUserId)
                                            .update("following", FieldValue.increment(1))

                                        db.collection("users").document(fromUserId)
                                            .update("followers", FieldValue.increment(1))

                                        db.collection("notifications")
                                            .add(
                                                hashMapOf(
                                                    "toUserId" to fromUserId,
                                                    "fromUserId" to currentUserId,
                                                    "type" to "follow_back",
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

                                        db.collection("users").document(fromUserId)
                                            .update("followers", FieldValue.increment(-1))

                                        // 🔥🔥🔥 NEW UNFOLLOW NOTIFICATION ADDED
                                        db.collection("notifications")
                                            .add(
                                                hashMapOf(
                                                    "toUserId" to fromUserId,
                                                    "fromUserId" to currentUserId,
                                                    "type" to "unfollow",
                                                    "timestamp" to System.currentTimeMillis(),
                                                    "seen" to false
                                                )
                                            )
                                    }
                                }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor =
                                if (isFollowing)
                                    Color.Gray
                                else
                                    Color(0xFF6A5AE0)
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (isFollowing) "Following"
                            else "Follow Back",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
