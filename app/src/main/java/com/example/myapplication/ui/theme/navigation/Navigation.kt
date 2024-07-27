package com.example.myapplication.ui.theme.navigation

import BurgerScreen
import NotificationScreen
import OrderHistoryScreen
import OrderHistoryViewModel
import OrderRepository
import PaymentScreen
import ProductDetailScreen
import SearchScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.ui.theme.CategoryScreens.ComboMealsScreen
import com.example.myapplication.ui.theme.CategoryScreens.DessertScreen
import com.example.myapplication.ui.theme.CategoryScreens.HealthyScreen
import com.example.myapplication.ui.theme.CategoryScreens.PizzaScreen
import com.example.myapplication.ui.theme.CategoryScreens.SaladScreen
import com.example.myapplication.ui.theme.model.Uploading
import com.example.myapplication.ui.theme.view.CreateAccountScreen
import com.example.myapplication.ui.theme.view.LoginScreen
import com.example.myapplication.ui.theme.view.OrderScreen
import com.example.myapplication.ui.theme.view.OrderViewModel
import com.example.myapplication.ui.theme.view.OtpVerifyScreen
import com.example.myapplication.ui.theme.view.WelcomeScreen
import com.example.myapplication.ui.theme.view.bottom.ConfirmPassword
import com.example.myapplication.ui.theme.view.bottom.EditProfileScreen
import com.example.myapplication.ui.theme.view.bottom.FavoriteScreen
import com.example.myapplication.ui.theme.view.bottom.HomeScreen
import com.example.myapplication.ui.theme.view.bottom.NotificationViewModel
import com.example.myapplication.ui.theme.view.bottom.ProfileScreen
import com.example.myapplication.ui.theme.view.bottom.SeeAllCategoriesScreen
import com.example.myapplication.ui.theme.view.bottom.UploadProductScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

@Composable
fun SetupNavGraph(
    navController: NavHostController,
    orderViewModel: OrderViewModel,
    auth: FirebaseAuth,
    orderHistoryViewModel: OrderHistoryViewModel,
    notificationViewModel: NotificationViewModel,
    firestore: FirebaseFirestore,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.WelcomeScreen.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(route = Screen.WelcomeScreen.route) {
            WelcomeScreen(navController)
        }
        composable(route = Screen.LoginScreen.route) {
            LoginScreen(navController = navController, onLoginSuccess = {
                navController.navigate(Screen.HomeScreen.route) {
                    popUpTo(Screen.WelcomeScreen.route) { inclusive = true }
                }
            })
        }
        composable(route = Screen.CreateAccountScreen.route) {
            CreateAccountScreen(navController)
        }
        composable(route = Screen.OtpVerifyScreen.route) {
            OtpVerifyScreen(navController)
        }
        composable(route = Screen.HomeScreen.route) {
            HomeScreen(navController = navController, orderViewModel = orderViewModel)
        }
        composable(route = Screen.FavoriteScreen.route) {
            FavoriteScreen(navController, orderViewModel)
        }
        composable(route = Screen.SearchScreen.route) {
            SearchScreen(navController)
        }
        composable(route = Screen.OrderScreen.route) {
            OrderScreen(navController = navController, orderViewModel = orderViewModel)
        }
        composable(route = Screen.ProfileScreen.route) {
            ProfileScreen(navController)
        }
        composable(route = Screen.ConfirmPasswordScreen.route) {
            ConfirmPassword(navController)
        }
        composable(route = Screen.EditingScreen.route) {
            EditProfileScreen(navController)
        }
        composable(
            "productDetail/{productJson}",
            arguments = listOf(navArgument("productJson") { type = NavType.StringType })
        ) { backStackEntry ->
            val productJson = backStackEntry.arguments?.getString("productJson")
            val product = Gson().fromJson(
                URLDecoder.decode(productJson, StandardCharsets.UTF_8.toString()),
                Uploading::class.java
            )
            ProductDetailScreen(navController, product, orderViewModel)
        }
        composable(route = Screen.ConfirmPaymentScreen.route) {
            PaymentScreen(navController, orderViewModel)
        }
        composable(route = Screen.SeeAllScreen.route) {
            SeeAllCategoriesScreen(navController)
        }
        composable(route = Screen.Burgers.route) {
            BurgerScreen(navController)
        }
        composable(route = Screen.Pizza.route) {
            PizzaScreen(navController)
        }
        composable(route = Screen.ComboMeals.route) {
            ComboMealsScreen(navController)
        }
        composable(route = Screen.Dessert.route) {
            DessertScreen(navController)
        }
        composable(route = Screen.Salad.route) {
            SaladScreen(navController)
        }
        composable(route = Screen.Healthy.route) {
            HealthyScreen(navController)
        }
        composable(Screen.NotificationScreen.route) {
            NotificationScreen(navController,notificationViewModel = notificationViewModel)
        }
        composable(Screen.OrderHistoryScreen.route) {
            val userId = auth.currentUser?.uid ?: ""
            val orderHistoryViewModel: OrderHistoryViewModel = viewModel(
                factory = OrderHistoryViewModel.OrderHistoryViewModelFactory(
                    OrderRepository(
                        firestore
                    ), userId
                )
            )
            OrderHistoryScreen(
                navController = navController,
                orderHistoryViewModel = orderHistoryViewModel
            )
        }
        composable(route = Screen.ProductUploadScreen.route) {
            UploadProductScreen(navController)
        }
    }
}
