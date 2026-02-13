package com.example.notify.auth

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class  AuthViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // 🔥 NEW FUNCTION ADDED (GOOGLE AUTO CREATE USER)
    fun createGoogleUserIfNotExists(
        onComplete: () -> Unit
    ) {
        val user = auth.currentUser ?: return
        val uid = user.uid

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                val fullName = user.displayName ?: ""
                val parts = fullName.trim().split(" ")

                val first = parts.firstOrNull() ?: ""
                val last = if (parts.size > 1)
                    parts.drop(1).joinToString(" ")
                else ""

                if (!doc.exists()) {

                    val userMap = hashMapOf(
                        "firstName" to first,
                        "surname" to last,
                        "email" to (user.email ?: ""),
                        "phone" to "",
                        "bio" to "",
                        "followers" to 0,
                        "following" to 0
                    )

                    db.collection("users")
                        .document(uid)
                        .set(userMap)
                        .addOnSuccessListener { onComplete() }

                }
                else if (!doc.contains("firstName")) {

                    val updateMap = hashMapOf(
                        "firstName" to first,
                        "surname" to last,
                        "email" to (user.email ?: "")
                    )

                    db.collection("users")
                        .document(uid)
                        .set(updateMap, SetOptions.merge())
                        .addOnSuccessListener { onComplete() }

                }
                else {
                    onComplete()
                }
            }
    }

    fun login(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError(it.localizedMessage ?: "Login failed")
            }
    }

    fun signup(
        firstName: String,
        surname: String,
        email: String,
        password: String,
        phone: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user!!.uid

                val userMap = hashMapOf(
                    "firstName" to firstName,
                    "surname" to surname,
                    "email" to email,
                    "phone" to phone
                )

                db.collection("users")
                    .document(uid)
                    .set(userMap)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener {
                        onError(it.localizedMessage ?: "Failed to save user")
                    }
            }
            .addOnFailureListener {
                onError(it.localizedMessage ?: "Signup failed")
            }
    }

    fun checkIfUsernameExists(
        onResult: (Boolean) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                onResult(doc.exists() && doc.contains("username"))
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun isUsernameAvailable(
        username: String,
        onResult: (Boolean) -> Unit
    ) {
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { query ->
                onResult(query.isEmpty)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }

    fun saveUsername(
        username: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return

        val data = hashMapOf(
            "username" to username
        )

        db.collection("users")
            .document(uid)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener {
                onError("Failed to save username")
            }
    }

    // 🔥🔥🔥 LOGOUT FUNCTION ADDED (NO LINE REMOVED)
    fun logout() {
        auth.signOut()
    }
}
