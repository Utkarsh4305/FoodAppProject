package com.example.myapplication.ui.theme.navigation

sealed class Screen(val route: String) {
    object WelcomeScreen : Screen("welcome_screen")
    object LoginScreen : Screen("login_screen")
    object CreateAccountScreen : Screen("create_account_screen")
    object OtpVerifyScreen : Screen("otp_verify_screen")
    object ForgotPasswordScreen : Screen("forgot_password_screen")
    object HomeScreen : Screen("home_screen")
    object FavoriteScreen : Screen("favorite_screen")
    object SearchScreen : Screen("search_screen")
    object OrderScreen : Screen("order_screen")
    object TrackOrderScreen : Screen("track_order_screen")
    object SavesScreen : Screen("saves_screen")
    object ProfileScreen : Screen("profile_screen")
    object ConfirmPasswordScreen : Screen("ConfirmPassword_screen")
    object EditingScreen : Screen("Editing_screen")
    object ProductDetailScreen : Screen("Product_Detail_Screen")
    object ConfirmPaymentScreen : Screen("Confirm_Payment_Screen")
    object SeeAllScreen : Screen("See_All_Screen")
    object Burgers : Screen("burgers")
    object ComboMeals : Screen("Combo_Meals")
    object Dessert : Screen("Dessert")
    object Healthy : Screen("Healthy")
    object Pizza : Screen("Pizza")
    object Salad : Screen("Salad")
    object NotificationScreen : Screen("Notification_Screen")
    object OrderHistoryScreen : Screen("Order_History_Screen")
    object ProductUploadScreen : Screen("Product_Upload_Screen")

}
