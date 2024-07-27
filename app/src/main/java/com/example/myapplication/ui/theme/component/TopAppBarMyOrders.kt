package com.example.myapplication.ui.theme.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapplication.ui.theme.navigation.Screen
import com.example.myapplication.ui.theme.theme.colorBlack

@Composable
fun TopAppBarMyOrders(navController: NavController) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = {navController.navigate(Screen.HomeScreen.route) }) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "",
                tint = colorBlack
            )

        }

        Text(
            text = "\t \t  My Orders ",
            color = colorBlack,
            style = MaterialTheme.typography.button,
            modifier = Modifier.padding(start = 100.dp)
        )
    }
}

/*@Composable
@Preview
fun TopAppBarMyOrdersPreview() {
    TopAppBarMyOrders(NavController)
}*/