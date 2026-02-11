package com.example.notify.profile

import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class ProfileViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

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

    // 🔥 NEW FUNCTION ADDED (USERNAME EXIST CHECK — SAME USER ALLOWED)
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
                    // allow if same user
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
    }
}
