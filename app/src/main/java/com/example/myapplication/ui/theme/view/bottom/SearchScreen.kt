import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.example.myapplication.ui.theme.model.Uploading
import com.example.myapplication.ui.theme.theme.colorRedDark
import com.example.myapplication.ui.theme.theme.colorWhite
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(navController: NavController, viewModel: SearchBarViewModel = viewModel()) {
    val query by viewModel.searchText.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val products by viewModel.products.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var searchQuery by remember { mutableStateOf("") }
    val filteredProducts = products.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isSystemInDarkTheme()) Color.Black else colorWhite)
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "Search Icon") },
                placeholder = { Text(text = "Search Items") },
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

            Spacer(modifier = Modifier.height(16.dp))
            if (isSearching) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
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
fun ProductItem(product: Uploading) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
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
                painter = rememberImagePainter(product.imageUrl),
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.name,
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Price: \$${product.price}",
                style = MaterialTheme.typography.body1,
                color = Color.Red
            )
        }
    }
}

@Composable
@Preview
fun SearchScreenPreview() {
    SearchScreen(navController = NavController(LocalContext.current))
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
fun SearchScreenDarkPreview() {
    SearchScreen(navController = NavController(LocalContext.current))
}
