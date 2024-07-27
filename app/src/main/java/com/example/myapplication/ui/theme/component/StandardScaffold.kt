package com.arvind.foodizone.component

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.ui.theme.component.StandardBottomNavItem
import com.example.myapplication.ui.theme.model.BottomNavItem
import com.example.myapplication.ui.theme.navigation.Screen
import com.example.myapplication.ui.theme.theme.colorRedDark
import com.example.myapplication.ui.theme.theme.colorWhite
import com.example.myapplication.ui.theme.theme.search
import com.example.myapplication.ui.theme.view.OrderViewModel

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun StandardScaffold(
    navController: NavController,
    orderViewModel: OrderViewModel,
    modifier: Modifier = Modifier,
    showBottomBar: Boolean = true,
    onFabClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    val orders by orderViewModel.orders.observeAsState(emptyList())
    val alertCount = orders.size

    val bottomNavItems = listOf(
        BottomNavItem(
            route = Screen.HomeScreen.route,
            icon = Icons.Outlined.Home,
            contentDescription = "Home"
        ),
        BottomNavItem(
            route = Screen.FavoriteScreen.route,
            icon = Icons.Outlined.Favorite,
            contentDescription = "Favorite"
        ),
        BottomNavItem(route = ""),
        BottomNavItem(
            route = Screen.OrderScreen.route,
            icon = Icons.Outlined.Lock,
            contentDescription = "Order",
            alertCount = alertCount
        ),
        BottomNavItem(
            route = Screen.ProfileScreen.route,
            icon = Icons.Outlined.Person,
            contentDescription = "Profile"
        )
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colors.surface,
                    cutoutShape = CircleShape,
                    elevation = 0.dp
                ) {
                    BottomNavigation(backgroundColor = colorWhite) {
                        bottomNavItems.forEachIndexed { index, bottomNavItem ->
                            StandardBottomNavItem(
                                icon = bottomNavItem.icon,
                                contentDescription = bottomNavItem.contentDescription,
                                selected = bottomNavItem.route == navController.currentDestination?.route,
                                alertCount = bottomNavItem.alertCount,
                                enabled = bottomNavItem.icon != null
                            ) {
                                if (navController.currentDestination?.route != bottomNavItem.route) {
                                    navController.navigate(bottomNavItem.route)
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            if (showBottomBar) {
                FloatingActionButton(
                    backgroundColor = search,
                    onClick = onFabClick
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = colorWhite
                    )
                }
            }
        },
        isFloatingActionButtonDocked = true,
        floatingActionButtonPosition = FabPosition.Center,
        modifier = modifier
    ) {
        content()
    }
}
