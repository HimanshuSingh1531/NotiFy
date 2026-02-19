package com.example.notify.post

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

@Composable
fun PostScreen() {

    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current

    /* 🔥 SEPARATE STATES (BUG FIX) */
    var postText by remember { mutableStateOf("") }          // text post
    var textCaption by remember { mutableStateOf("") }       // text caption
    var imageCaption by remember { mutableStateOf("") }      // image caption

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val imagePicker =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.GetContent()
        ) { uri -> selectedImageUri = uri }

    val scaleAnim by animateFloatAsState(
        targetValue = if (isLoading) 0.96f else 1f,
        animationSpec = tween(400, easing = FastOutSlowInEasing)
    )
    LaunchedEffect(isLoading) {
        if (isLoading && selectedImageUri == null) {
            imageCaption = ""
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0C0C12), Color(0xFF1A1A24))
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {

        Text("Create Post", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)

        Spacer(modifier = Modifier.height(24.dp))

        /* ================= TEXT POST CARD ================= */
        PremiumCard(scaleAnim) {

            Text("Text Post", color = Color(0xFF9FA8FF))

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = postText,
                onValueChange = { postText = it },
                placeholder = { Text("Write something...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors()
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = textCaption,
                onValueChange = { textCaption = it },
                placeholder = { Text("Text caption (optional)", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors()
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        /* ================= IMAGE POST CARD ================= */

        PremiumCard(scaleAnim) {

            Text("Image Post", color = Color(0xFF9FA8FF))

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = imageCaption,
                onValueChange = { imageCaption = it },
                placeholder = { Text("Image caption...", color = Color.Gray) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = fieldColors()
            )

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(
                visible = selectedImageUri != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut()
            ) {
                Column {
                    AsyncImage(
                        model = selectedImageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                    )

                    TextButton(onClick = { selectedImageUri = null }) {
                        Icon(Icons.Default.Close, null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Remove Image")
                    }
                }
            }

            Button(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5AE0))
            ) {
                Icon(Icons.Default.Image, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Select Image")
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        /* ================= FINAL POST BUTTON ================= */

        Button(
            onClick = {

                if (postText.isEmpty() && selectedImageUri == null) return@Button
                isLoading = true

                fun savePost(imageUrl: String?) {

                    if (imageUrl == null && imageCaption.isEmpty() && textCaption.isNotEmpty()) {
                        imageCaption = textCaption
                    }

                    val post = hashMapOf(
                        "userId" to currentUser?.uid,
                        "text" to postText,
                        "caption" to imageCaption, // 🔥 ONLY IMAGE CAPTION SAVED
                        "textCaption" to textCaption,
                        "imageUrl" to (imageUrl ?: ""),
                        "timestamp" to System.currentTimeMillis(),
                        "likes" to 0
                    )

                    db.collection("posts")
                        .add(post)
                        .addOnSuccessListener {
                            postText = ""
                            textCaption = ""
                            imageCaption = ""
                            selectedImageUri = null
                            isLoading = false
                        }
                        .addOnFailureListener { isLoading = false }
                }

                if (selectedImageUri != null) {
                    val bytes =
                        context.contentResolver.openInputStream(selectedImageUri!!)?.readBytes()
                    if (bytes != null) {
                        uploadToCloudinary(bytes, { savePost(it) }) { isLoading = false }
                    }
                } else savePost(null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A5AE0))
        ) {
            if (isLoading)
                CircularProgressIndicator(color = Color.White)
            else
                Text("Post", fontSize = 18.sp)
        }
    }
}

/* ================= PREMIUM CARD ================= */

@Composable
fun PremiumCard(scale: Float, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer(scaleX = scale, scaleY = scale),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E2A))
    ) {
        Column(modifier = Modifier.padding(20.dp), content = content)
    }
}

@Composable
fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Color(0xFF6A5AE0),
    unfocusedBorderColor = Color(0xFF3A3A4A),
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)

/* ================= CLOUDINARY SAME ================= */

fun uploadToCloudinary(
    imageBytes: ByteArray,
    onSuccess: (String) -> Unit,
    onError: () -> Unit
) {

    val client = OkHttpClient()

    val requestBody = MultipartBody.Builder()
        .setType(MultipartBody.FORM)
        .addFormDataPart(
            "file",
            "image.jpg",
            RequestBody.create("image/*".toMediaTypeOrNull(), imageBytes)
        )
        .addFormDataPart("upload_preset", "notify_profile")
        .build()

    val request = Request.Builder()
        .url("https://api.cloudinary.com/v1_1/de6erh571/image/upload")
        .post(requestBody)
        .build()

    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) = onError()
        override fun onResponse(call: Call, response: Response) {
            response.body?.string()?.let {
                onSuccess(JSONObject(it).getString("secure_url"))
            } ?: onError()
        }
    })
}
