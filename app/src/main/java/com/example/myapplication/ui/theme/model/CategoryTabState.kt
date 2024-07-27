package com.example.myapplication.ui.theme.model

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class CategoryTabsState(categories: List<Categories>) {
    var categories by mutableStateOf(categories)
    var selectedCategory by mutableStateOf<Categories?>(null)

    fun onCategorySelected(category: Categories) {
        selectedCategory = if (selectedCategory == category) null else category
    }

    fun clearSelection() {
        selectedCategory = null
    }
}