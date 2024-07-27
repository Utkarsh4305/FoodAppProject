import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.ui.theme.model.Uploading
import com.example.myapplication.ui.theme.navigation.Screen
import com.example.myapplication.ui.theme.theme.colorBlack
import com.example.myapplication.ui.theme.theme.colorRedDark
import com.example.myapplication.ui.theme.view.OrderViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun ProductDetailScreen(
    navController: NavHostController,
    product: Uploading,
    orderViewModel: OrderViewModel
) {
    val context = LocalContext.current
    var detailedProduct by remember { mutableStateOf<Uploading?>(null) }
    var quantity by remember { mutableStateOf(1) }
    var isFavorite by remember { mutableStateOf(false) }
    var isInCart by remember { mutableStateOf(false) }

    LaunchedEffect(product) {
        detailedProduct = fetchProductDetails(product.name)
        detailedProduct?.let { prod ->
            isFavorite = orderViewModel.isFavorite(
                MyOrders(
                    name = listOf(prod.name),
                    price = prod.price.toDouble(),
                    imageUrl = prod.imageUrl,
                    quantity = quantity
                )
            )
            isInCart = orderViewModel.isInCart(
                MyOrders(
                    name = listOf(prod.name),
                    price = prod.price.toDouble(),
                    imageUrl = prod.imageUrl,
                    quantity = quantity
                )
            )
        }
    }

    if (detailedProduct != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Image(
                painter = rememberAsyncImagePainter(detailedProduct!!.imageUrl),
                contentDescription = detailedProduct!!.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = detailedProduct!!.name,
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold,
                color = colorBlack
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Price: $${detailedProduct!!.price}",
                style = MaterialTheme.typography.h6,
                color = colorRedDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = detailedProduct!!.description,
                style = MaterialTheme.typography.body1,
                color = colorBlack
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (isInCart) {
                        // Remove from cart
                        detailedProduct?.let { product ->
                            orderViewModel.orders.value?.find { it.name.contains(product.name) }?.let { order ->
                                orderViewModel.removeOrder(order)

                            }
                        }
                    } else {
                        // Add to cart
                        val newOrder = MyOrders(
                            name = listOf(detailedProduct!!.name),
                            price = detailedProduct!!.price.toDouble(),
                            imageUrl = detailedProduct!!.imageUrl,
                            quantity = quantity
                        )
                        orderViewModel.addOrder(newOrder)

                    }
                    // Toggle the cart state
                    isInCart = !isInCart
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = colorRedDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(text = if (isInCart) "Remove from Cart" else "Add to Cart", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val order = MyOrders(
                        name = listOf(detailedProduct!!.name),
                        price = detailedProduct!!.price.toDouble(),
                        imageUrl = detailedProduct!!.imageUrl,
                        quantity = quantity
                    )
                    if (isFavorite) {
                        orderViewModel.removeFavorite(order)

                    } else {
                        orderViewModel.addFavorite(order)

                    }
                    isFavorite = !isFavorite
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = colorRedDark),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = if (isFavorite) "Remove from Favorites" else "Add to Favorites",
                    color = Color.White
                )
            }
        }
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = colorRedDark)
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
