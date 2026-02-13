package com.example.notify.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
) {
    object Home : BottomNavItem("home", Icons.Default.Home, "Home")
    object Message : BottomNavItem("message", Icons.Default.Mail, "Message")
    object Post : BottomNavItem("post", Icons.Default.Add, "Post")
    object Liked : BottomNavItem("liked", Icons.Default.FavoriteBorder, "Liked")
    object Profile : BottomNavItem("profile", Icons.Default.Person, "Profile")
}
