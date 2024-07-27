package com.example.myapplication.ui.theme.CategoryScreens

import CategoryCard
import MyOrders
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.model.Uploading
import com.example.myapplication.ui.theme.theme.colorRedDark
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class CategoryDessert(val name: String, val imageRes: Int)

@Composable
fun DessertScreen(navController: NavHostController) {
    var products by remember { mutableStateOf<List<Uploading>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            products = fetchProductsFromFirestoreDessert()
            isLoading = false
            Log.d("BurgerScreen", "Fetched products: $products")
        }
    }

    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon") },
            placeholder = { Text(text = "Search Desserts") },
            singleLine = true, // Make the TextField single-lined
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done // Set the IME action to Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    // Handle the Done action here, e.g., hide the keyboard or initiate a search
                    keyboardController?.hide()
                }
            ),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = colorRedDark.copy(alpha = 0.1f), // Light yellow background
                focusedIndicatorColor = colorRedDark, // Yellow border when focused
                unfocusedIndicatorColor = colorRedDark.copy(alpha = 0.5f), // Light yellow border when not focused
                textColor = Color.Black, // Black text color
                leadingIconColor = Color.Gray, // Gray color for the search icon
                placeholderColor = Color.Gray // Gray color for the placeholder text
            )
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(                modifier = Modifier.align(Alignment.Center),
                        color = colorRedDark)
                }
            }
            filteredProducts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No products found", textAlign = TextAlign.Center)
                }
            }
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(filteredProducts) { product ->
                        CategoryCard(product = product) {
                            val productJson = URLEncoder.encode(Gson().toJson(product), StandardCharsets.UTF_8.toString())
                            navController.navigate("productDetail/$productJson")
                        }
                    }
                }
            }
        }
    }
}

suspend fun fetchProductsFromFirestoreDessert(): List<Uploading> {
    val db = FirebaseFirestore.getInstance()
    return try {
        val snapshot: QuerySnapshot = db.collection("products")
            .whereEqualTo("category", "Desserts")
            .get()
            .await()
        Log.d("fetchProductsFromFirestore", "Documents fetched: ${snapshot.documents.size}")
        snapshot.documents.mapNotNull { document ->
            val price = document.get("price").toString().toDoubleOrNull() ?: 0.0
            val product = Uploading(
                description = document.getString("description") ?: "",
                imageUrl = document.getString("imageUrl") ?: "",
                name = document.getString("name") ?: "",
                price = price.toString(),
                category = document.getString("category") ?: ""
            )
            Log.d("fetchProductsFromFirestore", "Product: $product")
            product
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Log.e("fetchProductsFromFirestore", "Error fetching products", e)
        emptyList()
    }
}

@Preview(showBackground = true)
@Composable
fun DessertScreenPreview() {
    DessertScreen(navController = rememberNavController())
}
