package com.example.myapplication.ui.theme.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Badge
import androidx.compose.material.BadgedBox
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.theme.colorBlack

@Composable
fun TopAppBarMap() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { }) {
            Icon(
                imageVector = Icons.Outlined.ArrowBack,
                contentDescription = "",
                tint = colorBlack
            )

        }

        Text(
            text = "Track Order",
            color = colorBlack,
            style = MaterialTheme.typography.button
        )
        BadgedBox(
            badge = {
                Badge { Text("4") }
            },
            modifier = Modifier
                .padding(end = 20.dp)
        ) {
            Icon(
                Icons.Outlined.Lock,
                contentDescription = "Notification"
            )

        }


    }
}

@Composable
@Preview
fun TopAppBarMapPreview() {
    TopAppBarMap()
}
