    import android.util.Log
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.clickable
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.lazy.grid.GridCells
    import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
    import androidx.compose.foundation.lazy.grid.items
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.foundation.text.KeyboardActions
    import androidx.compose.foundation.text.KeyboardOptions
    import androidx.compose.material.*
    import androidx.compose.material.icons.Icons
    import androidx.compose.material.icons.filled.Search
    import androidx.compose.runtime.*
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.draw.clip
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.layout.ContentScale
    import androidx.compose.ui.text.input.ImeAction
    import androidx.compose.ui.text.style.TextAlign
    import androidx.compose.ui.unit.dp
    import androidx.navigation.NavHostController
    import coil.compose.rememberAsyncImagePainter
    import com.example.myapplication.ui.theme.model.Uploading
    import com.example.myapplication.ui.theme.theme.colorRedDark
    import com.google.firebase.firestore.FirebaseFirestore
    import com.google.gson.Gson
    import kotlinx.coroutines.launch
    import kotlinx.coroutines.tasks.await
    import java.net.URLEncoder
    import java.nio.charset.StandardCharsets
    import androidx.compose.ui.platform.LocalSoftwareKeyboardController
    import androidx.compose.ui.text.font.FontWeight
    import com.example.myapplication.ui.theme.theme.colorBlack

    @Composable
    fun BurgerScreen(navController: NavHostController) {
        var products by remember { mutableStateOf<List<Uploading>>(emptyList()) }
        var searchQuery by remember { mutableStateOf("") }
        var isLoading by remember { mutableStateOf(true) }
        val coroutineScope = rememberCoroutineScope()
        val keyboardController = LocalSoftwareKeyboardController.current
        val colorRedDark = Color(0xFFFF7B7B)

        LaunchedEffect(Unit) {
            coroutineScope.launch {
                products = fetchProductsFromFirestore()
                isLoading = false
                Log.d("BurgerScreen", "Fetched products: $products")
            }
        }

        val filteredProducts = products.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon") },
                placeholder = { Text(text = "Search Burgers") },
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
                            color = com.example.myapplication.ui.theme.theme.colorRedDark
                        )
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

    @Composable
    fun CategoryCard(product: Uploading, onClick: () -> Unit) {
        Card(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .wrapContentHeight()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(16.dp),
            elevation = 4.dp,
            backgroundColor = colorRedDark,
        ) {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colors.surface)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (product.imageUrl.isNotBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(product.imageUrl),
                        contentDescription = product.name,
                        modifier = Modifier
                            .size(130.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.h6,
                    color = colorBlack
                )
                //Text(text = product.uploadername, style = MaterialTheme.typography.subtitle2, color = colorRedDark)
                Text(
                    text = "$${product.price}",
                    style = MaterialTheme.typography.subtitle2,
                    color = Color.Gray
                )
            }
        }
    }

    suspend fun fetchProductsFromFirestore(): List<Uploading> {
        val db = FirebaseFirestore.getInstance()
        return try {
            val snapshot = db.collection("products")
                .whereEqualTo("category", "Burgers")
                .get()
                .await()
            Log.d("fetchProductsFromFirestore", "Documents fetched: ${snapshot.documents.size}")
            snapshot.documents.mapNotNull { document ->
                val product = document.toObject(Uploading::class.java)
                Log.d("fetchProductsFromFirestore", "Product: $product")
                product
            }
        } catch (e: Exception) {
            Log.e("fetchProductsFromFirestore", "Error fetching products", e)
            emptyList()
        }
    }