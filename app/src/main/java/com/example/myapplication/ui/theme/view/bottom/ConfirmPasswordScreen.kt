package com.example.myapplication.ui.theme.view.bottom

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import com.example.myapplication.ui.theme.navigation.Screen
import com.example.myapplication.ui.theme.theme.colorPurple400
import com.example.myapplication.ui.theme.theme.colorWhite

@Composable
fun ConfirmPassword(navController: NavController) {
    var Confirm  by remember { mutableStateOf("") }

    TextField(value = Confirm,
        leadingIcon = {
            Row(
                modifier = Modifier.wrapContentWidth(),
                verticalAlignment = Alignment.CenterVertically,
                content = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "",
                        tint = Color.Gray
                    )

                }
            )
        },
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = colorWhite,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        label = { Text(text = "Password") },
        shape = RoundedCornerShape(24.dp),
        onValueChange = {
            Confirm = it
        })
    Spacer(modifier = Modifier.height(16.dp))
    Column(
        modifier = Modifier.fillMaxWidth().
        background(colorPurple400),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        Button(onClick = { Screen.EditingScreen.route }) {
            Text(text = "Enter")
        }
    }
}




@Composable
@Preview
fun ConfirmPasswordScreenPreview() {
    ProfileScreen(navController = NavController(LocalContext.current))
}


@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
fun ConfirmPasswordScreenDarkPreview() {
    ProfileScreen(navController = NavController(LocalContext.current))
}
