package com.example.myapplication.ui.theme.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TopAppBarHome(
    onMenuClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onAddressClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val cuisines = listOf("Italian", "Chinese", "Indian", "Mexican", "American")
    var selectedCuisine by remember { mutableStateOf("Select Cuisine") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier
                .size(50.dp)
                .clip(shape = CircleShape)
                .background(Color.White),
            onClick = onMenuClick
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "",
                tint = Color.Black
            )
        }

        Box(
            modifier = Modifier
                .weight(0.85f)
                .padding(start = 20.dp, end = 20.dp)
        ) {
            TextField(
                modifier = Modifier
                    .height(50.dp)
                    .fillMaxWidth(),
                colors = TextFieldDefaults.textFieldColors(
                    backgroundColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                ),
                value = selectedCuisine,
                shape = RoundedCornerShape(24.dp),
                singleLine = true,
                onValueChange = { },
                readOnly = true,
                placeholder = {
                    Text(
                        text = "Select Cuisine",
                        color = Color.Black
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "",
                            tint = Color.Black
                        )
                    }
                }
            )

            // Invisible clickable overlay to open dropdown
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = { expanded = true })
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                cuisines.forEach { cuisine ->
                    DropdownMenuItem(onClick = {
                        selectedCuisine = cuisine
                        expanded = false
                    }) {
                        Text(text = cuisine)
                    }
                }
            }
        }

        IconButton(
            modifier = Modifier
                .size(50.dp)
                .clip(shape = CircleShape)
                .background(Color.White),
            onClick = onNotificationClick
        ) {
            Icon(
                imageVector = Icons.Outlined.Notifications,
                contentDescription = "",
                tint = Color.Black
            )
        }
    }
}
/*@Composable
@Preview
fun TopAppBarHomeScreenPreview() {
    TopAppBarHome()
}
*/