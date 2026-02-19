package com.example.notify.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue

@Composable
fun FollowListScreen(
    userId: String,
    type: String,
    navController: NavController
) {

    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var searchText by remember { mutableStateOf("") }
    var users by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var filteredUsers by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var myFollowingList by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // 🔥 My following real-time
    LaunchedEffect(Unit) {
        if (currentUserId.isNotEmpty()) {
            db.collection("follows")
                .whereEqualTo("followerId", currentUserId)
                .addSnapshotListener { snapshot, _ ->
                    myFollowingList =
                        snapshot?.documents?.mapNotNull {
                            it.getString("followingId")
                        } ?: emptyList()
                }
        }
    }

    // 🔥 Followers / Following real-time (FIXED STABLE VERSION)
    LaunchedEffect(userId, type) {

        isLoading = true

        val followQuery =
            if (type == "followers") {
                db.collection("follows")
                    .whereEqualTo("followingId", userId)
            } else {
                db.collection("follows")
                    .whereEqualTo("followerId", userId)
            }

        followQuery.addSnapshotListener { snapshot, _ ->

            val ids =
                snapshot?.documents?.mapNotNull { doc ->
                    if (type == "followers")
                        doc.getString("followerId")
                    else
                        doc.getString("followingId")
                } ?: emptyList()

            if (ids.isEmpty()) {
                users = emptyList()
                filteredUsers = emptyList()
                isLoading = false
                return@addSnapshotListener
            }

            val tempList = mutableListOf<Map<String, Any>>()

            ids.forEach { id ->

                db.collection("users")
                    .document(id)
                    .addSnapshotListener { userDoc, _ ->

                        userDoc?.data?.let { data ->

                            val userMap = data + ("uid" to userDoc.id)

                            if (tempList.none { it["uid"] == userDoc.id }) {
                                tempList.add(userMap)
                            }

                            if (tempList.size == ids.size) {
                                users = tempList.toList()
                                filteredUsers = users
                                isLoading = false
                            }
                        }
                    }
            }
        }
    }

    fun filter(query: String) {
        filteredUsers =
            users.filter {
                val username =
                    (it["username"] as? String ?: "").lowercase()
                username.contains(query.lowercase())
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

        Row(verticalAlignment = Alignment.CenterVertically) {

            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.White)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = if (type == "followers") "Followers" else "Following",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                filter(it)
            },
            placeholder = { Text("Search", color = Color.Gray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(30.dp),
            leadingIcon = {
                Icon(Icons.Default.Search, null, tint = Color.Gray)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6A5AE0),
                unfocusedBorderColor = Color.Gray,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF6A5AE0))
                }
            }

            filteredUsers.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text =
                            if (type == "followers")
                                "No followers yet"
                            else
                                "Not following anyone",
                        color = Color.Gray
                    )
                }
            }

            else -> {

                LazyColumn {

                    items(filteredUsers) { user ->

                        val username =
                            user["username"] as? String ?: ""
                        val photoUrl =
                            user["photoUrl"] as? String ?: ""
                        val uid =
                            user["uid"] as? String ?: ""

                        val isFollowing =
                            myFollowingList.contains(uid)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                                .clickable {
                                    navController.navigate("profile_view/$uid")
                                },
                            horizontalArrangement =
                                Arrangement.SpaceBetween,
                            verticalAlignment =
                                Alignment.CenterVertically
                        ) {

                            Row(verticalAlignment =
                                Alignment.CenterVertically) {

                                AsyncImage(
                                    model = photoUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(45.dp)
                                        .clip(CircleShape)
                                )

                                Spacer(modifier =
                                    Modifier.width(12.dp))

                                Text(
                                    username,
                                    color = Color.White,
                                    fontWeight =
                                        FontWeight.SemiBold
                                )
                            }

                            if (currentUserId != uid) {

                                val buttonText =
                                    when {
                                        isFollowing ->
                                            "Following"
                                        type == "followers" ->
                                            "Follow Back"
                                        else ->
                                            "Follow"
                                    }

                                Button(
                                    onClick = {

                                        db.collection("follows")
                                            .whereEqualTo("followerId",
                                                currentUserId)
                                            .whereEqualTo("followingId",
                                                uid)
                                            .get()
                                            .addOnSuccessListener { snapshot ->

                                                if (snapshot.isEmpty) {

                                                    db.collection("follows")
                                                        .add(
                                                            hashMapOf(
                                                                "followerId" to currentUserId,
                                                                "followingId" to uid,
                                                                "timestamp" to System.currentTimeMillis()
                                                            )
                                                        )

                                                    db.collection("users")
                                                        .document(currentUserId)
                                                        .update("following",
                                                            FieldValue.increment(1))

                                                    db.collection("users")
                                                        .document(uid)
                                                        .update("followers",
                                                            FieldValue.increment(1))

                                                } else {

                                                    snapshot.documents.forEach {
                                                        db.collection("follows")
                                                            .document(it.id)
                                                            .delete()
                                                    }

                                                    db.collection("users")
                                                        .document(currentUserId)
                                                        .update("following",
                                                            FieldValue.increment(-1))

                                                    db.collection("users")
                                                        .document(uid)
                                                        .update("followers",
                                                            FieldValue.increment(-1))
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
                                    Text(buttonText, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
