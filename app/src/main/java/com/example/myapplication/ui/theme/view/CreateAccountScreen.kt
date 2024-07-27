package com.example.myapplication.ui.theme.view

import android.app.Activity
import android.content.ContentValues.TAG
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.ui.theme.navigation.Screen
import com.example.myapplication.ui.theme.theme.colorPurple400
import com.example.myapplication.ui.theme.theme.colorRedLite
import com.example.myapplication.ui.theme.theme.colorWhite
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

@Composable
fun CreateAccountScreen(navController: NavController) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val passwordFocusRequester = remember { FocusRequester() }

    // Firebase Auth instance
    val auth = FirebaseAuth.getInstance()

    // Firebase Realtime Database reference
    val database = FirebaseDatabase.getInstance().reference

    // Initialize Google Sign-In client
    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("851373291683-54k7qave1fm4t8pfie8fqifap9tdpoj9.apps.googleusercontent.com") // Replace with your web client ID
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    // Activity result launcher for Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!, auth, database, navController, context)
            } catch (e: ApiException) {
                // Handle sign-in failure
                Log.e(TAG, "Google sign-in failed", e)
                Toast.makeText(context, "Google sign-in failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF7F00FF), Color(0xFFE100FF))
                )
            )
            .verticalScroll(rememberScrollState())
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .align(Alignment.BottomCenter)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            var username by remember { mutableStateOf("") }
            var userpassword by remember { mutableStateOf("") }

            TextField(
                value = username,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = "",
                        tint = Color.Gray
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = colorWhite,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(focusDirection = FocusDirection.Down) }
                ),
                label = { Text(text = "Email") },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                onValueChange = {
                    username = it
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                value = userpassword,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "",
                        tint = Color.Gray
                    )
                },
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = colorWhite,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        createAccount(
                            email = username,
                            password = userpassword,
                            auth = auth,
                            database = database,
                            navController = navController,
                            context = context
                        )
                    }
                ),
                visualTransformation = PasswordVisualTransformation(),
                label = { Text(text = "Password") },
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                onValueChange = {
                    userpassword = it
                }
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    createAccount(
                        email = username,
                        password = userpassword,
                        auth = auth,
                        database = database,
                        navController = navController,
                        context = context
                    )
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = colorRedLite),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "Create new account",
                    color = colorWhite,
                    style = MaterialTheme.typography.button,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    navController.navigate(Screen.LoginScreen.route)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    "Login",
                    color = colorWhite,
                    style = MaterialTheme.typography.button,
                    modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
                )
            }
        }
    }
}

private fun createAccount(
    email: String,
    password: String,
    auth: FirebaseAuth,
    database: DatabaseReference,
    navController: NavController,
    context: Context
) {
    if (email.isEmpty()) {
        Toast.makeText(context, "Email cannot be empty", Toast.LENGTH_SHORT).show()
        return
    }

    if (password.isEmpty()) {
        Toast.makeText(context, "Password cannot be empty", Toast.LENGTH_SHORT).show()
        return
    }

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Account creation succeeded
                val user = auth.currentUser
                val userId = user?.uid

                // Prepare user data to save in the database
                val userMap = hashMapOf(
                    "userId" to userId,
                    "email" to email
                )

                // Save user data in the database
                if (userId != null) {
                    database.child("users").child(userId).setValue(userMap)
                        .addOnCompleteListener { saveTask ->
                            if (saveTask.isSuccessful) {
                                // Data saved successfully, navigate to the home screen
                                navController.popBackStack()
                                navController.navigate(Screen.HomeScreen.route)
                            } else {
                                // Failed to save user data, display error message
                                val errorMessage = "${saveTask.exception?.message}"
                                Log.e(TAG, errorMessage)
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    val errorMessage = "Failed to get user ID"
                    Log.e(TAG, errorMessage)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } else {
                // Account creation failed, display error message
                val errorMessage = "${task.exception?.message}"
                Log.e(TAG, errorMessage)
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
}

private fun firebaseAuthWithGoogle(
    idToken: String,
    auth: FirebaseAuth,
    database: DatabaseReference,
    navController: NavController,
    context: Context
) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Sign-in succeeded
                val user = auth.currentUser
                val userId = user?.uid
                val email = user?.email

                // Prepare user data to save in the database
                val userMap = hashMapOf(
                    "userId" to userId,
                    "email" to email
                )

                // Save user data in the database
                if (userId != null && email != null) {
                    database.child("users").child(userId).setValue(userMap)
                        .addOnCompleteListener { saveTask ->
                            if (saveTask.isSuccessful) {
                                // Data saved successfully, navigate to the home screen
                                navController.popBackStack()
                                navController.navigate(Screen.HomeScreen.route)
                            } else {
                                // Failed to save user data, display error message
                                val errorMessage = "${saveTask.exception?.message}"
                                Log.e(TAG, errorMessage)
                                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    val errorMessage = "Failed to get user ID or email"
                    Log.e(TAG, errorMessage)
                    Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                }
            } else {
                // Sign-in failed, display error message
                val errorMessage = "${task.exception?.message}"
                Log.e(TAG, errorMessage)
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewCreateAccountScreen() {
    CreateAccountScreen(NavController(LocalContext.current))
}
