package com.example.notify.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

// 🔥 REMOVED FirebaseStorage
// import com.google.firebase.storage.FirebaseStorage

// 🔥 NEW IMPORTS ADDED (Cloudinary)
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.launch
import java.io.File
import com.example.notify.network.RetrofitInstance

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // ❌ REMOVED STORAGE INSTANCE
    // private val storage = FirebaseStorage.getInstance()

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    fun loadUserData(
        onSuccess: (Map<String, Any>) -> Unit
    ) {
        val uid = getCurrentUserId() ?: return

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                doc.data?.let { onSuccess(it) }
            }
    }

    // 🔥 USERNAME EXIST CHECK — SAME USER ALLOWED
    fun checkUsernameAvailability(
        username: String,
        onResult: (Boolean) -> Unit
    ) {
        val currentUid = getCurrentUserId() ?: return

        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { query ->

                if (query.isEmpty) {
                    onResult(true)
                } else {
                    val sameUser = query.documents.any { it.id == currentUid }
                    onResult(sameUser)
                }
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun updateProfile(
        firstName: String,
        surname: String,
        bio: String,
        phone: String,
        username: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = getCurrentUserId() ?: return

        if (!username.startsWith("@")) {
            onError("Username must start with @")
            return
        }

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                val oldUsername = doc.getString("username") ?: ""
                val lastChange = doc.getTimestamp("lastUsernameChange")

                if (username != oldUsername) {

                    if (lastChange != null) {

                        val daysPassed =
                            TimeUnit.MILLISECONDS.toDays(
                                System.currentTimeMillis() - lastChange.toDate().time
                            )

                        if (daysPassed < 28) {
                            onError("You can change your username again after 28 days.")
                            return@addOnSuccessListener
                        }
                    }
                }

                val updates = hashMapOf<String, Any>(
                    "firstName" to firstName,
                    "surname" to surname,
                    "bio" to bio,
                    "phone" to phone,
                    "username" to username,
                    "lastUsernameChange" to Timestamp.now()
                )

                db.collection("users")
                    .document(uid)
                    .update(updates)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener {
                        onError("Failed to update profile")
                    }
            }
            .addOnFailureListener {
                onError("Failed to load user data")
            }
    }

    // 🔥🔥🔥 CLOUDINARY PROFILE IMAGE UPLOAD (FREE)
    fun uploadProfileImage(
        file: File,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {

        val requestFile =
            file.asRequestBody("image/*".toMediaTypeOrNull())

        val body =
            MultipartBody.Part.createFormData("file", file.name, requestFile)

        val preset =
            "notify_profile".toRequestBody("text/plain".toMediaTypeOrNull())

        viewModelScope.launch {

            try {

                val response =
                    RetrofitInstance.api.uploadImage(body, preset)

                if (response.isSuccessful) {

                    val imageUrl =
                        response.body()?.secure_url ?: ""

                    if (imageUrl.isNotEmpty()) {

                        val uid = getCurrentUserId() ?: return@launch

                        db.collection("users")
                            .document(uid)
                            .update("photoUrl", imageUrl)
                            .addOnSuccessListener { onSuccess() }
                            .addOnFailureListener {
                                onError("Failed to save image URL")
                            }
                    }

                } else {
                    onError("Image upload failed")
                }

            } catch (e: Exception) {
                onError(e.message ?: "Upload error")
            }
        }
    }

    // 🔥🔥🔥 NEW FUNCTION ADDED — LOGOUT
    fun logout(
        onSuccess: () -> Unit
    ) {
        auth.signOut()
        onSuccess()
    }
}
