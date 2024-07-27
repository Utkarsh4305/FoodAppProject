package com.example.myapplication

import OrderHistoryViewModel

import OrderRepository
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.arvind.foodizone.component.StandardScaffold
import com.example.myapplication.ui.theme.navigation.Screen
import com.example.myapplication.ui.theme.navigation.SetupNavGraph
import com.example.myapplication.ui.theme.theme.FoodizoneTheme
import com.example.myapplication.ui.theme.view.OrderViewModel
import com.example.myapplication.ui.theme.view.bottom.NotificationViewModel
import com.example.myapplication.ui.theme.view.bottom.NotificationViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    private lateinit var orderRepository: OrderRepository
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Initialize Firestore with persistence enabled
        firestore = Firebase.firestore
        firestore.firestoreSettings = firestoreSettings {
            isPersistenceEnabled = true
        }

        // Initialize the repository here
        orderRepository = OrderRepository(firestore)

        setContent {
            FoodizoneTheme {
                val navController = rememberNavController()
                val orderViewModel: OrderViewModel = viewModel()

                // Get the current userId, or use an empty string if not available
                val userId = auth.currentUser?.uid ?: ""

                // Create the factory with both orderRepository and userId
                val factory =
                    OrderHistoryViewModel.OrderHistoryViewModelFactory(orderRepository, userId)
                val orderHistoryViewModel: OrderHistoryViewModel = viewModel(factory = factory)

                // Create the NotificationViewModelFactory
                val notificationFactory = NotificationViewModelFactory(auth, firestore)
                val notificationViewModel: NotificationViewModel = viewModel(factory = notificationFactory)

                val navBackStackEntry by navController.currentBackStackEntryAsState()

                val bottomBarRoutes = listOf(
                    Screen.HomeScreen.route,
                    Screen.FavoriteScreen.route,
                    Screen.OrderScreen.route,
                    Screen.ProfileScreen.route,
                )

                Surface(color = MaterialTheme.colors.background) {
                    StandardScaffold(
                        navController = navController,
                        orderViewModel = orderViewModel,
                        showBottomBar = navBackStackEntry?.destination?.route in bottomBarRoutes,
                        modifier = Modifier.fillMaxSize(),
                        onFabClick = {
                            navController.navigate(Screen.SearchScreen.route)
                        }
                    ) {
                        SetupNavGraph(
                            navController = navController,
                            orderViewModel = orderViewModel,
                            auth = auth,
                            orderHistoryViewModel = orderHistoryViewModel,
                            notificationViewModel = notificationViewModel,
                            firestore = firestore
                        )
                    }
                }
            }
        }
    }
}
