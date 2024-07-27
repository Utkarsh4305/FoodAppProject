package com.example.myapplication.ui.theme.view.bottom

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberImagePainter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

@Composable
fun UploadProductScreen(navController: NavController) {
    var productName by remember { mutableStateOf("") }
    var productDescription by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var uploaderName by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedCategory by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val categories = listOf("Burgers", "Pizza", "Combo Meals", "Desserts", "Salad", "Healthy")
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        imageUri = uri
    }

    val focusRequesterName = remember { FocusRequester() }
    val focusRequesterDescription = remember { FocusRequester() }
    val focusRequesterPrice = remember { FocusRequester() }
    val focusRequesterUploaderName = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F7))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Upload Product",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TextField(
            value = productName,
            onValueChange = { if (!isLoading) productName = it },
            label = { Text("Product Name") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequesterName),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
            enabled = !isLoading,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusRequesterDescription.requestFocus() }
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = productDescription,
            onValueChange = { if (!isLoading) productDescription = it },
            label = { Text("Product Description") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequesterDescription),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
            enabled = !isLoading,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusRequesterPrice.requestFocus() }
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = productPrice,
            onValueChange = { if (!isLoading) productPrice = it },
            label = { Text("Product Price") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequesterPrice),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(
                onNext = { focusRequesterUploaderName.requestFocus() }
            ),
            enabled = !isLoading,
            singleLine = true
        )
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = uploaderName,
            onValueChange = { if (!isLoading) uploaderName = it },
            label = { Text("Uploader Name") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequesterUploaderName),
            colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.White),
            enabled = !isLoading,
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        var expanded by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(enabled = !isLoading) { expanded = !expanded }) {
            Text(
                text = if (selectedCategory.isEmpty()) "Select Category" else selectedCategory,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                color = if (isLoading) Color.Gray else Color.Black
            )
        }
        DropdownMenu(
            expanded = expanded && !isLoading,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(onClick = {
                    selectedCategory = category
                    expanded = false
                }) {
                    Text(text = category)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .background(Color.Gray)
                .clickable(enabled = !isLoading) { imagePickerLauncher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            imageUri?.let {
                Image(
                    painter = rememberImagePainter(it),
                    contentDescription = "Product Image",
                    modifier = Modifier.size(128.dp)
                )
            } ?: Text(text = "Select Image", color = Color.White)
        }
        Spacer(modifier = Modifier.height(24.dp))

        GradientButton(
            text = "Upload Product",
            //enabled = !isLoading
        ) {
            isLoading = true
            uploadProduct(
                context = context,
                productName = productName,
                productDescription = productDescription,
                productPrice = productPrice,
                category = selectedCategory,
                uploaderName = uploaderName,
                imageUri = imageUri,
                onSuccess = {
                    isLoading = false
                    Toast.makeText(context, "Product uploaded successfully", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                },
                onFailure = {
                    isLoading = false
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}



private fun uploadProduct(
    context: Context,
    productName: String,
    productDescription: String,
    productPrice: String,
    category: String,
    uploaderName: String,
    imageUri: Uri?,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    if (imageUri == null) {
        onFailure(Exception("Image not selected"))
        return
    }

    if (category.isEmpty()) {
        onFailure(Exception("Category not selected"))
        return
    }

    val storageReference = FirebaseStorage.getInstance().reference.child("products/${UUID.randomUUID()}.jpg")
    storageReference.putFile(imageUri)
        .addOnSuccessListener { taskSnapshot ->
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                val downloadUrl = uri.toString()
                val productData = hashMapOf(
                    "name" to productName,
                    "description" to productDescription,
                    "price" to productPrice,
                    "category" to category,
                    "uploaderName" to uploaderName,
                    "imageUrl" to downloadUrl
                )
                FirebaseFirestore.getInstance().collection("products")
                    .add(productData)
                    .addOnSuccessListener { onSuccess() }
                    .addOnFailureListener { onFailure(it) }
            }
        }
        .addOnFailureListener { onFailure(it) }
}

@Preview(showBackground = true)
@Composable
fun UploadProductScreenPreview() {
    UploadProductScreen(navController = NavController(LocalContext.current))
}
