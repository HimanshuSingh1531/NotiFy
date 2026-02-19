package com.example.notify.home
import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import androidx.navigation.NavController

@Composable
fun HomeScreen(
    navController: NavController
) {

    var searchText by remember { mutableStateOf("") }
    var allUsers by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var filteredUsers by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var followingList by remember { mutableStateOf<List<String>>(emptyList()) }
    var posts by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    LaunchedEffect(Unit) {

        db.collection("users").get().addOnSuccessListener { result ->
            allUsers = result.documents.mapNotNull { doc ->
                doc.data?.plus("uid" to doc.id)
            }
        }

        if (currentUserId.isNotEmpty()) {
            db.collection("follows")
                .whereEqualTo("followerId", currentUserId)
                .addSnapshotListener { snapshot, _ ->
                    followingList = snapshot?.documents?.mapNotNull {
                        it.getString("followingId")
                    } ?: emptyList()
                }
        }

        db.collection("posts")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                posts = snapshot?.documents?.mapNotNull {
                    it.data?.plus("postId" to it.id)
                } ?: emptyList()
            }
    }

    fun filterUsers(query: String) {
        val cleanQuery = query.removePrefix("@").trim().lowercase()
        filteredUsers = if (cleanQuery.isEmpty()) emptyList()
        else allUsers.filter {
            (it["username"] as? String ?: "").lowercase().contains(cleanQuery)
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

        Text("Notify", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)

        Spacer(modifier = Modifier.height(20.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {

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
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            IconButton(onClick = { filterUsers(searchText) }) {
                Icon(Icons.Default.Search, null, tint = Color(0xFF6A5AE0))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn {

            // SEARCH USERS (UNCHANGED)

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
                        modifier = Modifier.clickable {
                            navController.navigate("profile_view/$followedUserId")
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

                        Text(username, color = Color.White)
                    }

                    Button(
                        onClick = {
                            // FOLLOW SYSTEM RESTORED
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
                            containerColor = if (isFollowing) Color.Gray else Color(0xFF6A5AE0)
                        )
                    ) {
                        Text(if (isFollowing) "Following" else "Follow")
                    }
                }
            }

            // POSTS

            items(
                items = posts,
                key = { it["postId"] as String }
            ) { post ->

            val postUserId = post["userId"] as? String ?: ""
                val text = post["text"] as? String ?: ""
                val imageUrl = post["imageUrl"] as? String ?: ""
                val postId = post["postId"] as? String ?: ""
                val caption = post["caption"] as? String ?: ""
                var likes by remember { mutableStateOf(post["likes"] as? Long ?: 0) }
                var shares by remember { mutableStateOf(post["shares"] as? Long ?: 0) }
                var commentCount by remember { mutableStateOf(0) }

                // 🔥 REAL TIME COMMENT COUNT LISTENER
                LaunchedEffect(postId) {
                    db.collection("posts")
                        .document(postId)
                        .collection("comments")
                        .addSnapshotListener { snapshot, _ ->
                            commentCount = snapshot?.size() ?: 0
                        }
                }


                var username by remember { mutableStateOf("") }
                var profileUrl by remember { mutableStateOf("") }
                var liked by rememberSaveable(postId) { mutableStateOf(false) }

                LaunchedEffect(postId, currentUserId) {
                    if (currentUserId.isNotEmpty() && postId.isNotEmpty()) {
                        db.collection("posts")
                            .document(postId)
                            .collection("likes")
                            .document(currentUserId)
                            .addSnapshotListener { snapshot, _ ->
                                liked = snapshot?.exists() == true
                            }
                    }
                }
                LaunchedEffect(postId) {
                    db.collection("posts")
                        .document(postId)
                        .addSnapshotListener { snapshot, _ ->
                            shares = snapshot?.getLong("shares") ?: 0
                        }
                }
                LaunchedEffect(postUserId) {
                    db.collection("users").document(postUserId).get()
                        .addOnSuccessListener {
                            username = it.getString("username") ?: ""
                            profileUrl = it.getString("photoUrl") ?: ""
                        }
                }
                val shareLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        db.collection("posts")
                            .document(postId)
                            .update("shares", FieldValue.increment(1))
                        shares += 1
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {

                    // PROFILE + USERNAME ROW ADDED

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                navController.navigate("profile_view/$postUserId")
                            }
                    ) {

                        AsyncImage(
                            model = profileUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(username, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    if (caption.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = caption,
                            color = Color(0xFFDDDDDD),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(start = 50.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1F1F2A))
                            .padding(12.dp)
                    ) {

                        if (text.isNotEmpty()) {
                            Text(text, color = Color.White)
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        if (imageUrl.isNotEmpty()) {
                            AsyncImage(
                                model = imageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        IconButton(
                            onClick = {

                                if (!liked) {

                                    // ✅ ADD LIKE DOCUMENT (per user)
                                    db.collection("posts")
                                        .document(postId)
                                        .collection("likes")
                                        .document(currentUserId)
                                        .set(mapOf("likedAt" to System.currentTimeMillis()))

                                    // ✅ INCREMENT LIKE COUNT ONCE
                                    db.collection("posts")
                                        .document(postId)
                                        .update("likes", FieldValue.increment(1))

                                    liked = true
                                    likes += 1

                                } else {

                                    // ❌ REMOVE LIKE DOCUMENT
                                    db.collection("posts")
                                        .document(postId)
                                        .collection("likes")
                                        .document(currentUserId)
                                        .delete()

                                    // ❌ DECREMENT LIKE COUNT
                                    db.collection("posts")
                                        .document(postId)
                                        .update("likes", FieldValue.increment(-1))

                                    liked = false
                                    likes -= 1
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (liked)
                                    Icons.Filled.Favorite
                                else
                                    Icons.Outlined.FavoriteBorder,
                                contentDescription = null,
                                tint = if (liked) Color.Red else Color.White
                            )
                        }

                        Text("$likes", color = Color.White, modifier = Modifier.padding(end = 16.dp))

                        IconButton(
                            onClick = {
                                navController.navigate("comments/$postId")
                            }
                        ) {
                            Icon(Icons.Default.ChatBubbleOutline, null, tint = Color.White)
                        }

                       // 🔥 REAL TIME COMMENT COUNT
                        Text(
                            "$commentCount",
                            color = Color.White,
                            modifier = Modifier.padding(end = 16.dp)
                        )

                        IconButton(onClick = {

                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    if (text.isNotEmpty()) text else imageUrl
                                )
                            }

                            val chooser = Intent.createChooser(intent, "Share post")
                            shareLauncher.launch(chooser)

                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }

                        Text("$shares", color = Color.White)
                    }
                }
            }
        }
    }
}
