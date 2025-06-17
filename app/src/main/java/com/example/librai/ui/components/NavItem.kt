package com.example.librai.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(val route: String, val icon: ImageVector, val label: String)

val bottomNavItems = listOf(
    NavItem("home", Icons.Default.Home, "Home"),
    NavItem("library", Icons.AutoMirrored.Filled.MenuBook, "Library"),
    NavItem("profile", Icons.Default.Person, "Profile")
)
