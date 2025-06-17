package com.example.librai.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.librai.ui.components.bottomNavItems
import com.example.librai.ui.theme.MintBackground
import com.example.librai.ui.theme.PrimaryColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavBar(
    currentRoute: String,
    onItemSelected: (String) -> Unit,
) {
    Surface(
        // This paints the full rectangle *then* clips it
        color = MintBackground,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        tonalElevation = 0.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        NavigationBar (
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,              // flat look
            modifier = Modifier.fillMaxSize()
        ){
            bottomNavItems.forEach { item ->
                NavigationBarItem(
                    icon = { Icon(item.icon, contentDescription = item.label) },
                    selected = currentRoute == item.route,
                    onClick = { onItemSelected(item.route) },
                    label = { Text(item.label) },
                    colors = NavigationBarItemDefaults.colors(
                        // here you control selected/unselected colors
                        selectedIconColor   = PrimaryColor,
                        selectedTextColor   = PrimaryColor,
                        unselectedIconColor = PrimaryColor.copy(alpha = 0.6f),
                        unselectedTextColor = PrimaryColor.copy(alpha = 0.6f),
                        indicatorColor      = Color.Transparent // no pill
                    )
                )
            }
        }
    }
}