package com.aiphoneguardian.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.aiphoneguardian.app.ui.navigation.Screen
import com.aiphoneguardian.app.ui.theme.*

@Composable
fun BottomNavBar(navController: NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val items = listOf(
        BottomNavItem("الرئيسية", Icons.Default.Home, Screen.Dashboard.route, NeonCyan),
        BottomNavItem("فحص", Icons.Default.QrCodeScanner, Screen.Scanner.route, NeonPurple),
        BottomNavItem("رسائل", Icons.Default.Message, Screen.Messages.route, NeonGreen),
        BottomNavItem("مكالمات", Icons.Default.Phone, Screen.Calls.route, Color(0xFFFF9800)),
        BottomNavItem("AI", Icons.Default.SmartToy, Screen.AIChat.route, Color(0xFFE91E63)),
        BottomNavItem("إعدادات", Icons.Default.Settings, Screen.Settings.route, TextMuted),
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .shadow(elevation = 16.dp, spotColor = NeonCyan.copy(alpha = 0.1f)),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(SpaceDark.copy(alpha = 0.95f), SpaceCard.copy(alpha = 0.98f))
                    )
                )
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavItem(item = item, isSelected = isSelected) {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(Screen.Dashboard.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                }
            }
        }
    }
}

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String,
    val color: Color = Color.White
)

@Composable
private fun NavItem(item: BottomNavItem, isSelected: Boolean, onClick: () -> Unit) {
    val iconSize by animateDpAsState(
        targetValue = if (isSelected) 24.dp else 20.dp,
        label = "iconSize"
    )
    val glowColor = if (isSelected) item.color else TextMuted

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .padding(4.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(
                    color = if (isSelected) item.color.copy(alpha = 0.18f) else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = glowColor,
                modifier = Modifier.size(iconSize)
            )
        }
        Text(
            text = item.label,
            color = glowColor,
            fontSize = 9.sp,
            maxLines = 1
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .padding(top = 2.dp)
                    .width(16.dp)
                    .height(2.dp)
                    .background(color = item.color, shape = RoundedCornerShape(1.dp))
            )
        }
    }
}
