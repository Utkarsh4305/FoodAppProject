package com.example.myapplication.ui.theme.view.bottom

import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.R
import com.example.myapplication.ui.theme.navigation.Screen
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore("profile_prefs")

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    var email by remember { mutableStateOf(currentUser?.email ?: "Fetching...") }
    var name by remember { mutableStateOf("Fetching...") }
    var showDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showImagePickerDialog by remember { mutableStateOf(false) }
    val dataStoreHelper = DataStoreHelper(context)
    val scope = rememberCoroutineScope()
    var selectedImageRes by remember { mutableIntStateOf(R.drawable.pixel) }
    var selectedImageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            email = currentUser.email ?: "No Email"
            name = currentUser.displayName ?: "No Name"
        }

        dataStoreHelper.profileImageFlow.collect { imageRes ->
            selectedImageRes = imageRes
        }
    }

    if (showDialog) {
        PasswordConfirmationDialog(
            onDismiss = { showDialog = false },
            onConfirm = { password ->
                confirmPassword(password, auth, navController) { errorMessage ->
                    if (errorMessage != null) {
                        // Handle error message display
                    } else {
                        showDialog = false
                    }
                }
            }
        )
    }

    if (showImagePickerDialog) {
        ImagePickerDialog(
            onDismiss = { showImagePickerDialog = false },
            onImageSelected = { imageRes ->
                selectedImageRes = imageRes
                scope.launch {
                    dataStoreHelper.saveProfileImage(imageRes)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .padding(16.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color.Gray, CircleShape)
                .padding(4.dp)
                .clickable { showImagePickerDialog = true } // Open image picker dialog on click
        ) {
            Image(
                painter = painterResource(id = selectedImageRes),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = email,
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        ProfileOptionCard(title = "Order History", onClick = { navController.navigate(Screen.OrderHistoryScreen.route) })
        ProfileOptionCard(title = "Upload Product", onClick = { navController.navigate(Screen.ProductUploadScreen.route) })
        Spacer(modifier = Modifier.height(24.dp))
        GradientButton(text = "Edit Profile", onClick = { showDialog = true })
        Spacer(modifier = Modifier.height(24.dp))
        GradientButton(text = "Log Out", onClick = { showLogoutDialog = true })
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Confirm Logout") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                Button(onClick = {
                    logout(auth, navController)
                    showLogoutDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(onClick = { showLogoutDialog = false }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun PasswordConfirmationDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Confirm Password") },
        text = {
            Column {
                Text(text = "Please enter your current password to proceed.")
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null // Reset error message on value change
                    },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    isError = errorMessage != null,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (password.text.isBlank()) {
                                errorMessage = "Password cannot be empty"
                            } else {
                                onConfirm(password.text)
                            }
                        }
                    )
                )
                errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = message,
                        color = MaterialTheme.colors.error,
                        style = MaterialTheme.typography.caption,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (password.text.isBlank()) {
                    errorMessage = "Password cannot be empty"
                } else {
                    onConfirm(password.text)
                }
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(12.dp),
        backgroundColor = MaterialTheme.colors.surface
    )
}
@Composable
fun ProfileOptionCard(title: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Forward Arrow",
                tint = Color.Gray
            )
        }
    }
}

private fun confirmPassword(
    password: String,
    auth: FirebaseAuth,
    navController: NavController,
    onResult: (String?) -> Unit
) {
    val user = auth.currentUser
    if (user != null && user.email != null) {
        val credential = EmailAuthProvider.getCredential(user.email!!, password)
        user.reauthenticate(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                navController.navigate(Screen.EditingScreen.route)
                onResult(null)
            } else {
                onResult(task.exception?.message)
            }
        }
    } else {
        onResult("User not found")
    }
}

private fun logout(auth: FirebaseAuth, navController: NavController) {
    auth.signOut()
    navController.navigate(Screen.LoginScreen.route) {
        popUpTo(Screen.ProfileScreen.route) {
            inclusive = true
        }
    }
}

@Composable
fun GradientButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(48.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF8330C6), Color(0xFF7C4DFF))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun ImagePickerDialog(onDismiss: () -> Unit, onImageSelected: (Int) -> Unit) {
    val imageOptions = listOf(
        R.drawable.pixel,
        R.drawable.pixel1,
        R.drawable.pixel2,
        R.drawable.pixel3 ,
        R.drawable.pixel4,
        R.drawable.pixel5,
        R.drawable.pixel6,
        R.drawable.pixel7,
        R.drawable.pixel8,
        R.drawable.pixel9,
        R.drawable.pixel10,
        R.drawable.pixel12,
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose Profile Image") },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(300.dp) // Set a fixed height to make it scrollable
            ) {
                items(imageOptions.size) { index ->
                    ImagePickerItem(
                        imageRes = imageOptions[index],
                        index = index,
                        onImageSelected = {
                            onImageSelected(it)
                            onDismiss()
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(12.dp),
        backgroundColor = MaterialTheme.colors.surface
    )
}

@Composable
fun ImagePickerItem(imageRes: Int, index: Int, onImageSelected: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onImageSelected(imageRes)
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Image Option $index",
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape)
                .padding(4.dp)
        )
    }
}
class DataStoreHelper(private val context: Context) {

    companion object {
        private val PROFILE_IMAGE_KEY = intPreferencesKey("profile_image")
    }

    private val dataStore = context.dataStore

    val profileImageFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[PROFILE_IMAGE_KEY] ?: R.drawable.pixel
    }

    suspend fun saveProfileImage(imageRes: Int) {
        dataStore.edit { preferences ->
            preferences[PROFILE_IMAGE_KEY] = imageRes
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    // Provide a preview NavController
    val navController = rememberNavController()
    ProfileScreen(navController = navController)
}
