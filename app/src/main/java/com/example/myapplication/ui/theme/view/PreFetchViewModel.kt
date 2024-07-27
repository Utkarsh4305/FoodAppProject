package com.example.myapplication.ui.theme.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.myapplication.ui.theme.view.bottom.fetchOfferProducts
import com.example.myapplication.ui.theme.view.bottom.fetchPopularProducts
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import android.content.Context
import androidx.lifecycle.ViewModelProvider

class PrefetchViewModel(private val context: Context) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    fun prefetchHomeScreenData() {
        viewModelScope.launch {
            prefetchPopularProducts()
            prefetchOfferProducts()
        }
    }

    private suspend fun prefetchPopularProducts() {
        // Use the existing fetchPopularProducts function
        val products = fetchPopularProducts()
        products.forEach { product ->
            prefetchImage(product.imageUrl)
        }
    }

    private suspend fun prefetchOfferProducts() {
        // Use the existing fetchOfferProducts function
        val products = fetchOfferProducts()
        products.forEach { product ->
            prefetchImage(product.imageUrl)
        }
    }

    private fun prefetchImage(url: String) {
        val request = ImageRequest.Builder(context)
            .data(url)
            .build()
        ImageLoader(context).enqueue(request)
    }
}

class PrefetchViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PrefetchViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PrefetchViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}