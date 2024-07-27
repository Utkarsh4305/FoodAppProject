package com.example.myapplication.ui.theme.view.bottom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.ui.theme.navigation.Screen
import com.google.firebase.auth.FirebaseAuth

@Composable
fun EditProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var newEmail by remember { mutableStateOf(currentUser?.email ?: "") }
    var newPassword by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Text(
            text = "Edit Profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = newEmail,
            onValueChange = { newEmail = it },
            label = { Text("New Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextField(
            value = newPassword,
            onValueChange = { newPassword = it },
            label = { Text("New Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    updateUserProfile(newEmail, newPassword, auth, navController)
                }
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = {
                updateUserProfile(newEmail, newPassword, auth, navController)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
        ) {
            Text(text = "Update Profile", color = Color.White, fontSize = 16.sp)
        }
    }
}

private fun updateUserProfile(newEmail: String, newPassword: String, auth: FirebaseAuth, navController: NavController) {
    val user = auth.currentUser
    if (user != null) {
        val updates = mutableListOf<() -> Unit>()
        if (newEmail.isNotEmpty() && newEmail != user.email) {
            updates.add {
                user.updateEmail(newEmail).addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        // Handle email update failure
                    }
                }
            }
        }
        if (newPassword.isNotEmpty()) {
            updates.add {
                user.updatePassword(newPassword).addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        // Handle password update failure
                    }
                }
            }
        }
        updates.forEach { it.invoke() }
        navController.navigate(Screen.ProfileScreen.route)
    }
}
