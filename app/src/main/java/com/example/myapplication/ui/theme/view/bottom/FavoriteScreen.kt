package com.example.myapplication.ui.theme.view.bottom

import MyOrders
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.myapplication.ui.theme.model.Uploading
import com.example.myapplication.ui.theme.view.OrderViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun FavoriteScreen(
    navController: NavHostController,
    orderViewModel: OrderViewModel
) {
    val favorites by orderViewModel.favorites.observeAsState(emptyList())
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(favorites) {
        if (favorites.isNotEmpty() || isLoading) {
            isLoading = false
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // CircularProgressIndicator removed
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp, 16.dp, 16.dp, 64.dp)
            ) {
                items(favorites) { favorite ->
                    var itemLoading by remember { mutableStateOf(true) }

                    FavoriteItem(
                        favorite,
                        navController,
                        orderViewModel,
                        onLoadingChange = { loading -> itemLoading = loading }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}


@Composable
fun FavoriteItem(
    myOrders: MyOrders,
    navController: NavHostController,
    orderViewModel: OrderViewModel,
    onLoadingChange: (Boolean) -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Fetch product details when clicked
                CoroutineScope(Dispatchers.Main).launch {
                    onLoadingChange(true)
                    val productDetails = withContext(Dispatchers.IO) {
                        fetchProductDetails(myOrders.name.joinToString(", "))
                    }
                    onLoadingChange(false)
                    productDetails?.let {
                        val productJson = URLEncoder.encode(Gson().toJson(it), StandardCharsets.UTF_8.toString())
                        navController.navigate("productDetail/$productJson")
                    } ?: run {
                        Toast.makeText(context, "Failed to load product details", Toast.LENGTH_SHORT).show()
                    }
                }
            },
        shape = RoundedCornerShape(12.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberImagePainter(myOrders.imageUrl),
                contentDescription = myOrders.name.joinToString(", "),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(325.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = myOrders.name.joinToString(", "),
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Price: $${myOrders.price}",
                style = MaterialTheme.typography.body1,
                color = Color.Red
            )
        }
    }
}



suspend fun fetchProductDetails(productName: String): Uploading? {
    val db = FirebaseFirestore.getInstance()
    val snapshot = db.collection("products")
        .whereEqualTo("name", productName)
        .get()
        .await()
    Log.d("fetchProductDetails", "Documents fetched: ${snapshot.documents.size}")
    return snapshot.documents.mapNotNull { document ->
        val price = document.get("price").toString().toDoubleOrNull() ?: 0.0
        val product = Uploading(
            description = document.getString("description") ?: "",
            imageUrl = document.getString("imageUrl") ?: "",
            name = document.getString("name") ?: "",
            price = price.toString(),
            category = document.getString("category") ?: ""
        )
        Log.d("fetchProductDetails", "Product: $product")
        product
    }.firstOrNull()
}

