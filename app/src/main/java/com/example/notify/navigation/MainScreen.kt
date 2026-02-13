package com.example.notify.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notify.profile.ProfileScreen
import com.example.notify.profile.ProfileViewModel
import com.example.notify.profile.EditProfileScreen
import com.example.notify.home.HomeScreen
import com.example.notify.message.MessageScreen
import com.example.notify.post.PostScreen
import com.example.notify.liked.LikedScreen

// 🔥 NEW IMPORTS ADDED (NOT REMOVING ANYTHING)
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.nestedscroll.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.fillMaxWidth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {

    val navController = rememberNavController()
    val profileViewModel: ProfileViewModel = viewModel()

    var showEdit by remember { mutableStateOf(false) }

    var bottomBarVisible by remember { mutableStateOf(true) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {

                if (available.y < -5) {
                    bottomBarVisible = false
                }

                if (available.y > 5) {
                    bottomBarVisible = true
                }

                return Offset.Zero
            }
        }
    }

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Message,
        BottomNavItem.Post,
        BottomNavItem.Liked,
        BottomNavItem.Profile
    )

    if (showEdit) {
        EditProfileScreen(
            profileViewModel = profileViewModel,
            onSave = {
                showEdit = false
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier.nestedScroll(nestedScrollConnection),

        // 🔥🔥🔥 THIS COMPLETELY REMOVES WHITE BACKGROUND
        containerColor = Color(0xFF0E0E13),

        bottomBar = {

            AnimatedVisibility(
                visible = bottomBarVisible,
                enter = slideInVertically(
                    animationSpec = tween(300),
                    initialOffsetY = { it }
                ),
                exit = slideOutVertically(
                    animationSpec = tween(300),
                    targetOffsetY = { it }
                )
            ) {

                NavigationBar(
                    containerColor = Color(0xFF0E0E13),
                    tonalElevation = 0.dp, // 🔥 remove extra elevation shadow
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 12.dp)
                        .clip(RoundedCornerShape(28.dp))
                ) {

                    val currentRoute =
                        navController.currentBackStackEntryAsState().value?.destination?.route

                    items.forEach { item ->

                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId)
                                    launchSingleTop = true
                                }
                            },
                            icon = {
                                Icon(
                                    item.icon,
                                    contentDescription = item.label
                                )
                            },
                            label = {
                                Text(item.label)
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color(0xFF6A5AE0),
                                selectedTextColor = Color(0xFF6A5AE0),
                                unselectedIconColor = Color.Gray,
                                unselectedTextColor = Color.Gray,
                                indicatorColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->

        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {

            composable("home") {
                HomeScreen()
            }

            composable("message") {
                MessageScreen()
            }

            composable("post") {
                PostScreen()
            }

            composable("liked") {
                LikedScreen()
            }

            composable("profile") {
                ProfileScreen(
                    profileViewModel = profileViewModel,
                    onEditClick = {
                        showEdit = true
                    },
                    onLogout = {
                        onLogout()
                    }
                )
            }
        }
    }
}
