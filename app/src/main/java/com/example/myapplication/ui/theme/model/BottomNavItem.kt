package com.example.myapplication.ui.theme.model


import android.icu.text.CaseMap.Title
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val icon: ImageVector? = null,
    val contentDescription: String? = null,
    val alertCount: Int? = null,
)
