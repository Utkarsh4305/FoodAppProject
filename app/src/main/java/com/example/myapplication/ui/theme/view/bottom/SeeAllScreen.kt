package com.example.myapplication.ui.theme.view.bottom

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.R
import com.example.myapplication.ui.theme.navigation.Screen

data class Category(val name: String, val imageRes: Int, val route: Screen)

@Composable
fun SeeAllCategoriesScreen(navController: NavHostController) {
    val allItems = listOf(
        Category("Burgers", R.drawable.burger6, Screen.Burgers),
        Category("Pizza", R.drawable.pizza2, Screen.Pizza),
        Category("Healthy", R.drawable.healthy3, Screen.Healthy),
        Category("Salad", R.drawable.salad1, Screen.Salad),
        Category("Dessert", R.drawable.desserts1, Screen.Dessert),
        Category("Combo Meals", R.drawable.combo_meals2, Screen.ComboMeals)
    )

    var searchQuery by remember { mutableStateOf("") }
    val filteredItems = allItems.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon"
                )
            },
            placeholder = { Text(text = "Search categories") },
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(filteredItems.size) { index ->
                CategoryCard(navController, category = filteredItems[index])
            }
        }
    }
}

@Composable
fun CategoryCard(navController: NavHostController, category: Category) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable {
                navController.navigate(category.route.route)
            },
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colors.surface)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = category.imageRes),
                contentDescription = category.name,
                modifier = Modifier.size(130.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = category.name)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SeeAllCategoriesScreenPreview() {
    SeeAllCategoriesScreen(navController = rememberNavController())
}
