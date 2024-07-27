package com.example.myapplication.ui.theme.view.bottom

import com.example.myapplication.ui.theme.model.Uploading
import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.compose.rememberImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.myapplication.R
import com.example.myapplication.ui.theme.component.TopAppBarHome
import com.example.myapplication.ui.theme.navigation.Screen
import com.example.myapplication.ui.theme.theme.*
import com.example.myapplication.ui.theme.view.MyApplication
import com.example.myapplication.ui.theme.view.OrderViewModel
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.QuerySnapshot
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import coil.imageLoader
import java.io.File
import java.io.FileOutputStream
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

@Composable
fun HomeScreen(navController: NavHostController, orderViewModel: OrderViewModel = viewModel()) {
    val context = LocalContext.current
    val imageLoader = (context.applicationContext as MyApplication).imageLoader

    var popularProducts by remember { mutableStateOf<List<Uploading>>(emptyList()) }
    var offerProducts by remember { mutableStateOf<List<Uploading>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            if (shouldUpdateCollections()) {
                updateDailyCollections()  // Update the collections daily
            }
            popularProducts = fetchPopularProducts() // Fetch popular products
            offerProducts = fetchOfferProducts()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isSystemInDarkTheme()) Color.Black else colorWhite)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 56.dp)
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .offset(0.dp, (-30).dp),
            painter = painterResource(id = R.drawable.bg_main),
            contentDescription = "Header Background",
            contentScale = ContentScale.FillWidth
        )

        Column(
            modifier = Modifier
                .padding(20.dp)
        ) {
            TopAppBarHome(
                onMenuClick = {
                    // Handle menu click
                },
                onNotificationClick = {
                    navController.navigate(Screen.NotificationScreen.route)
                },
                onAddressClick = {
                    // Handle address click
                }
            )
            Spacer(modifier = Modifier.height(30.dp))
            Title()
            Spacer(modifier = Modifier.height(20.dp))
            Content(navController, popularProducts, offerProducts, imageLoader)
        }
    }
}

@Composable
fun Title() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "What would you like to\n" +
                    "\t \t \t \t \t \t \t eat today ",
            color = colorBlack,
            style = MaterialTheme.typography.h6
        )
    }
}

@Composable
fun Content(
    navController: NavHostController,
    popularProducts: List<Uploading>,
    offerProducts: List<Uploading>,
    imageLoader: ImageLoader
) {
    Column {
        Spacer(modifier = Modifier.height(16.dp))
        CategorySection(navController)
        Spacer(modifier = Modifier.height(20.dp))
        PopularSection(navController, popularProducts, imageLoader)
        Spacer(modifier = Modifier.height(20.dp))
        OfferDealSection(navController, offerProducts, imageLoader)
    }
}

@Composable
fun CategorySection(navController: NavController) {
    Column {
        val itemList = listOf("Burgers", "Pizza", "Healthy", "Dessert")
        val categoryScreens = listOf(
            Screen.Burgers.route,
            Screen.Pizza.route,
            Screen.Healthy.route,
            Screen.Dessert.route,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Categories",
                color = colorBlack,
                style = MaterialTheme.typography.h6
            )
            TextButton(
                onClick = {
                    navController.navigate(Screen.SeeAllScreen.route)
                }
            ) {
                Text(
                    text = "See all",
                    color = colorRedDark
                )
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowRight,
                    contentDescription = "Localized description",
                    modifier = Modifier.padding(end = 8.dp),
                    tint = colorRedDark
                )
            }
        }

        LazyRow(modifier = Modifier.fillMaxWidth()) {
            items(itemList.size) { item ->
                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(color = colorRedGrayLight)
                        .clickable {
                            navController.navigate(categoryScreens[item])
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp)
                            .fillMaxHeight(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = itemList[item],
                            color = Color.Black
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
            }
        }
    }
}

@Composable
fun PopularSection(navController: NavController, popularProducts: List<Uploading>, imageLoader: ImageLoader) {
    val context = LocalContext.current

    Column {
        Text(
            text = "Popular now 🔥",
            style = MaterialTheme.typography.h6,
            color = colorBlack
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(5) { index ->
                if (index < popularProducts.size) {
                    val product = popularProducts[index]
                    val fileName = "${product.name}.png"
                    val file = File(context.filesDir, fileName)

                    if (!file.exists()) {
                        scheduleImageDownload(context, product.imageUrl, product.name)
                    }

                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .wrapContentHeight()
                            .padding(10.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(color = colorRedGrayLight)
                            .clickable {
                                val productJson = URLEncoder.encode(Gson().toJson(product), StandardCharsets.UTF_8.toString())
                                navController.navigate("productDetail/$productJson")
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            if (file.exists()) {
                                Image(
                                    painter = rememberImagePainter(file),
                                    contentDescription = "",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(160.dp)
                                )
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(product.imageUrl)
                                        .crossfade(true)
                                        .diskCachePolicy(CachePolicy.ENABLED)
                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                        .networkCachePolicy(CachePolicy.ENABLED)
                                        .build(),
                                    contentDescription = "",
                                    contentScale = ContentScale.Crop,
                                    imageLoader = imageLoader,
                                    modifier = Modifier.size(160.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.h6,
                                color = colorBlack
                            )
                            Text(
                                text = "$${product.price}",
                                style = MaterialTheme.typography.subtitle2,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(240.dp)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(color = colorRedGrayLight)
                    )
                }
            }
        }
    }
}

@Composable
fun OfferDealSection(navController: NavController, offerProducts: List<Uploading>, imageLoader: ImageLoader) {
    val context = LocalContext.current

    Column {
        Text(
            text = "Offer deals 🎉",
            style = MaterialTheme.typography.h6,
            color = colorBlack
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(4) { index ->
                if (index < offerProducts.size) {
                    val product = offerProducts[index]
                    val fileName = "${product.name}.png"
                    val file = File(context.filesDir, fileName)

                    if (!file.exists()) {
                        scheduleImageDownload(context, product.imageUrl, product.name)
                    }

                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .wrapContentHeight()
                            .padding(10.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(color = colorRedGrayLight)
                            .clickable {
                                val productJson = URLEncoder.encode(Gson().toJson(product), StandardCharsets.UTF_8.toString())
                                navController.navigate("productDetail/$productJson")
                            }
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            if (file.exists()) {
                                Image(
                                    painter = rememberImagePainter(file),
                                    contentDescription = "",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.size(160.dp)
                                )
                            } else {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(product.imageUrl)
                                        .crossfade(true)
                                        .diskCachePolicy(CachePolicy.ENABLED)
                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                        .networkCachePolicy(CachePolicy.ENABLED)
                                        .build(),
                                    contentDescription = "",
                                    contentScale = ContentScale.Crop,
                                    imageLoader = imageLoader,
                                    modifier = Modifier.size(160.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = product.name,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.h6,
                                color = colorBlack
                            )
                            Text(
                                text = "$${product.price}",
                                style = MaterialTheme.typography.subtitle2,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .width(200.dp)
                            .height(240.dp)
                            .padding(10.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(color = colorRedGrayLight)
                    )
                }
            }
        }
    }
}


@Composable
fun ImageLoaderView(imageName: String) {
    val context = LocalContext.current
    val file = File(context.filesDir, "$imageName.png")
    if (file.exists()) {
        Image(
            painter = rememberImagePainter(file),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(160.dp)
        )
    } else {
        CircularProgressIndicator(color = colorRedDark)
    }
}


suspend fun fetchPopularProducts(): List<Uploading> {
    val firestore = FirebaseFirestore.getInstance()
    val countRef = firestore.collection("count")

    return try {
        // Fetch top 5 products with the highest count
        val snapshot = countRef.orderBy("count", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .await()

        // Map the documents to Uploading objects
        snapshot.documents.mapNotNull { document ->
            val productName = document.id // Assuming the document ID is the product name
            fetchProductByName(productName)
        }
    } catch (e: Exception) {
        emptyList()
    }
}

// Helper function to fetch product details by name
suspend fun fetchProductByName(name: String): Uploading? {
    val firestore = FirebaseFirestore.getInstance()
    return try {
        val snapshot = firestore.collection("products")
            .whereEqualTo("name", name)
            .get()
            .await()

        snapshot.documents.firstOrNull()?.toObject(Uploading::class.java)
    } catch (e: Exception) {
        null
    }
}

suspend fun fetchOfferProducts(): List<Uploading> {
    return fetchProductsFromFirestore("Offer")
}

suspend fun fetchProductsFromFirestore(collectionName: String): List<Uploading> {
    val firestore = FirebaseFirestore.getInstance()
    return try {
        val snapshot = firestore.collection(collectionName).get().await()
        snapshot.documents.mapNotNull { it.toObject(Uploading::class.java) }
    } catch (e: Exception) {
        emptyList()
    }
}



suspend fun updateDailyCollections() {
    if (!shouldUpdateCollections()) {
        println("Collections are already updated for today.")
        return
    }

    val firestore = FirebaseFirestore.getInstance()
    val productsRef = firestore.collection("products")
    val popularRef = firestore.collection("Popular")
    val offerRef = firestore.collection("Offer")
    val updateTimestampRef = firestore.collection("metadata").document("lastUpdate")

    try {
        val productsSnapshot: QuerySnapshot = productsRef.get().await()
        val products = productsSnapshot.documents.mapNotNull { it.toObject(Uploading::class.java) }

        fun getRandomProducts(products: List<Uploading>, num: Int): List<Uploading> {
            return products.shuffled().take(num)
        }

        val randomPopularProducts = getRandomProducts(products, 4)
        val randomOfferProducts = getRandomProducts(products, 4)

        // Clear the collections before updating
        clearCollection(popularRef)
        clearCollection(offerRef)

        // Add new random products to Popular collection
        randomPopularProducts.forEach { popularRef.add(it) }

        // Add new random products to Offer collection
        randomOfferProducts.forEach { offerRef.add(it) }

        // Update the timestamp
        updateTimestampRef.set(mapOf("timestamp" to FieldValue.serverTimestamp()))

        println("Popular and Offer collections have been updated successfully.")
    } catch (e: Exception) {
        println("Error updating collections: $e")
    }
}


suspend fun clearCollection(ref: CollectionReference) {
    val snapshot = ref.get().await()
    for (doc in snapshot.documents) {
        ref.document(doc.id).delete().await()
    }
}
suspend fun shouldUpdateCollections(): Boolean {
    val firestore = FirebaseFirestore.getInstance()
    val updateTimestampRef = firestore.collection("metadata").document("lastUpdate")

    return try {
        val document = updateTimestampRef.get().await()
        if (document.exists()) {
            val lastUpdate = document.getDate("timestamp") ?: return true
            val currentTime = System.currentTimeMillis()
            val oneDayInMillis = 24 * 60 * 60 * 1000 // One day in milliseconds

            return (currentTime - lastUpdate.time) > oneDayInMillis
        } else {
            true
        }
    } catch (e: Exception) {
        true
    }
}

class ImageDownloadWorker(context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        val imageUrl = inputData.getString("imageUrl") ?: return Result.failure()
        val imageName = inputData.getString("imageName") ?: return Result.failure()

        return try {
            val loader = applicationContext.imageLoader
            val request = ImageRequest.Builder(applicationContext)
                .data(imageUrl)
                .build()

            // Use 'execute' within a coroutine context
            val result = loader.execute(request)

            val drawable = result.drawable
            if (drawable is BitmapDrawable) {
                val bitmap = drawable.bitmap
                saveBitmapAsDrawable(applicationContext, bitmap, imageName)
                Result.success(workDataOf("filePath" to "${applicationContext.filesDir}/$imageName.png"))
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private fun saveBitmapAsDrawable(context: Context, bitmap: Bitmap, name: String) {
        val file = File(context.filesDir, "$name.png")
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        out.flush()
        out.close()
    }
}


class ClearDrawablesWorker(context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        return try {
            val directory = applicationContext.filesDir
            val files = directory.listFiles()
            files?.forEach { file ->
                if (file.name.endsWith(".png")) {
                    file.delete()
                }
            }
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}

fun scheduleImageDownload(context: Context, imageUrl: String, imageName: String) {
    val downloadWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<ImageDownloadWorker>()
        .setInputData(workDataOf("imageUrl" to imageUrl, "imageName" to imageName))
        .build()
    WorkManager.getInstance(context).enqueue(downloadWorkRequest)
}


fun scheduleDailyClear(context: Context) {
    val clearWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<ClearDrawablesWorker>()
        .setInitialDelay(24, TimeUnit.HOURS)
        .build()
    WorkManager.getInstance(context).enqueue(clearWorkRequest)
}


data class Count(
    @PropertyName("count") val count: Long = 0
)

@Preview(
    name = "Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    widthDp = 360,
    heightDp = 640
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
    widthDp = 360,
    heightDp = 640
)
@Composable
fun HomeScreenPreview() {
    val navController = rememberNavController()
    HomeScreen(navController)
}
