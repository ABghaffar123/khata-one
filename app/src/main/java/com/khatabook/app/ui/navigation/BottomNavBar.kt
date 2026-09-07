package com.khatabook.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.khatabook.app.ui.responsive.LocalResponsiveTypography
import com.khatabook.app.ui.responsive.LocalWindowSize
import com.khatabook.app.ui.theme.ErrorRed
import com.khatabook.app.ui.theme.KhataGradientPresets

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Home.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem(Screen.Customers.route, "Customers", Icons.Filled.People, Icons.Outlined.People),
    BottomNavItem(Screen.Inventory.route, "Inventory", Icons.Filled.Inventory, Icons.Outlined.Inventory),
    BottomNavItem(Screen.Suppliers.route, "Suppliers", Icons.Filled.Business, Icons.Outlined.Business),
    BottomNavItem(Screen.More.route, "More", Icons.Filled.MoreHoriz, Icons.Outlined.MoreHoriz)
)

@Composable
fun KhataAdaptiveNavigation(
    navController: NavController,
    lowStockCount: Int = 0
) {
    val windowSize = LocalWindowSize.current
    if (windowSize.showNavigationRail) {
        KhataNavigationRail(navController = navController, lowStockCount = lowStockCount)
    } else {
        KhataBottomNavBar(navController = navController, lowStockCount = lowStockCount)
    }
}

@Composable
private fun KhataBottomNavBar(navController: NavController, lowStockCount: Int = 0) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val typography = LocalResponsiveTypography.current
    val gradientTheme = KhataGradientPresets.FireOrange

    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                icon = {
                    Box {
                        Icon(
                            if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label
                        )
                        // Bell badge on Inventory tab for low stock count
                        if (item.route == Screen.Inventory.route && lowStockCount > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(16.dp),
                                containerColor = ErrorRed,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = if (lowStockCount > 99) "99+" else "$lowStockCount",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = typography.labelSm,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                selected = selected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = gradientTheme.gradientStart,
                    selectedTextColor = gradientTheme.gradientStart,
                    indicatorColor = gradientTheme.gradientStart.copy(alpha = 0.12f)
                ),
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun KhataNavigationRail(navController: NavController, lowStockCount: Int = 0) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val typography = LocalResponsiveTypography.current
    val gradientTheme = KhataGradientPresets.FireOrange

    NavigationRail(modifier = Modifier) {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.route
            NavigationRailItem(
                icon = {
                    Box {
                        Icon(
                            if (selected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label
                        )
                        // Bell badge on Inventory tab for low stock count
                        if (item.route == Screen.Inventory.route && lowStockCount > 0) {
                            Badge(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(16.dp),
                                containerColor = ErrorRed,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = if (lowStockCount > 99) "99+" else "$lowStockCount",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        fontSize = typography.labelSm,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                selected = selected,
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = gradientTheme.gradientStart,
                    selectedTextColor = gradientTheme.gradientStart,
                    indicatorColor = gradientTheme.gradientStart.copy(alpha = 0.12f)
                ),
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(Screen.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
