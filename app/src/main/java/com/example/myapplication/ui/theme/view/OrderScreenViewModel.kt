package com.example.myapplication.ui.theme.view

import com.google.firebase.analytics.FirebaseAnalytics

import MyOrders
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

data class Product(
    val name: String,
    val price: Double,
    val imageId: Int,
    val subTitle: String
)

class OrderViewModel(application: Application) : AndroidViewModel(application) {
    private val _orders = MutableLiveData<List<MyOrders>>(emptyList())
    val orders: LiveData<List<MyOrders>> get() = _orders

    private val _favorites = MutableLiveData<List<MyOrders>>(emptyList())
    val favorites: LiveData<List<MyOrders>> get() = _favorites

    private val _popularItems = MutableStateFlow<List<Product>>(emptyList())
    val popularItems: StateFlow<List<Product>> = _popularItems

    private val _offerItems = MutableStateFlow<List<Product>>(emptyList())
    val offerItems: StateFlow<List<Product>> = _offerItems

    private val _recommendations = MutableStateFlow<List<Product>>(emptyList())
    val recommendations: StateFlow<List<Product>> = _recommendations

    private val _grandTotal = MutableLiveData<Double>(0.0)
    val grandTotal: LiveData<Double> = _grandTotal

    private val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(application)

    // Flag to control loading orders
    private var shouldLoadOrders = true

    init {
        // Load initial data from Firestore when ViewModel is created
        auth.currentUser?.let { user ->
            loadOrders(user.uid)
            loadFavorites(user.uid)
        }
    }

    fun updateOrderQuantity(order: MyOrders, newQuantity: Int) {
        val currentOrders = _orders.value?.toMutableList() ?: mutableListOf()
        val index = currentOrders.indexOfFirst {
            it.name == order.name &&
                    it.price == order.price &&
                    it.imageUrl == order.imageUrl
        }
        if (index != -1) {
            val updatedOrder = order.copy(quantity = newQuantity)
            currentOrders[index] = updatedOrder
            _orders.value = currentOrders
            // Update Firestore
            auth.currentUser?.let { user ->
                updateFirestoreOrder(user.uid, updatedOrder)
            }
        }
    }

    fun removeOrder(order: MyOrders) {
        val currentOrders = _orders.value?.toMutableList() ?: mutableListOf()
        currentOrders.remove(order)
        _orders.value = currentOrders
        // Remove from Firestore
        auth.currentUser?.let { user ->
            removeFirestoreOrder(user.uid, order)
        }
    }

    fun addOrder(order: MyOrders) {
        val currentList = _orders.value ?: emptyList()
        val updatedList = currentList.toMutableList().apply {
            add(order)
        }
        _orders.value = updatedList
        // Allow orders to be loaded again when a new item is added
        shouldLoadOrders = true
        // Add to Firestore
        auth.currentUser?.let { user ->
            addFirestoreOrder(user.uid, "", order) // Payment method will be updated later
        }
    }

    fun addFavorite(order: MyOrders) {
        val currentList = _favorites.value ?: emptyList()
        val updatedList = currentList.toMutableList().apply {
            add(order)
        }
        _favorites.value = updatedList
        // Add to Firestore
        auth.currentUser?.let { user ->
            addFirestoreFavorite(user.uid, order)

            logFavoriteEvent("add_favorite", order)
        }
    }

    fun removeFavorite(order: MyOrders) {
        val currentFavorites = _favorites.value?.toMutableList() ?: mutableListOf()
        currentFavorites.remove(order)
        _favorites.value = currentFavorites
        // Remove from Firestore
        auth.currentUser?.let { user ->
            removeFirestoreFavorite(user.uid, order)

            logFavoriteEvent("remove_favorite", order)
        }
    }

    fun confirmOrder(paymentMethod: String) {
        val orderList = orders.value
        val user = auth.currentUser ?: return

        if (orderList.isNullOrEmpty()) {
            Log.e("OrderViewModel", "No orders to confirm")
            return
        }

        updatePaymentMethod(user.uid, paymentMethod) { success ->
            if (success) {
                val currentGrandTotal = grandTotal.value ?: 0.0
                val orderData = hashMapOf(
                    "userId" to user.uid,
                    "items" to orderList.map { order ->
                        mapOf(
                            "name" to order.name,
                            "price" to order.price,
                            "quantity" to order.quantity,
                            "paymentMethod" to paymentMethod
                        )
                    },
                    "totalAmount" to currentGrandTotal,
                    "timestamp" to FieldValue.serverTimestamp()
                )

                firestore.collection("confirmedOrders").add(orderData)
                    .addOnSuccessListener { documentReference ->
                        Log.d(
                            "OrderViewModel",
                            "Order confirmed and added to Confirmed Orders in Firestore with ID: ${documentReference.id}"
                        )
                        updateProductCount(orderList)
                        clearOrders()
                        clearFirestoreOrders(user.uid)
                    }
                    .addOnFailureListener { e ->
                        Log.e(
                            "OrderViewModel",
                            "Error confirming order and adding to Confirmed Orders in Firestore",
                            e
                        )
                    }
            } else {
                Log.e("OrderViewModel", "Error updating payment method in Firestore")
            }
        }
    }



    private fun addFirestoreOrder(userId: String, paymentMethod: String, order: MyOrders) {
        val orderCollection = firestore.collection("orders")
        val orderData = hashMapOf(
            "userId" to userId,
            "name" to order.name,
            "price" to order.price,
            "quantity" to order.quantity,
            "imageUrl" to order.imageUrl, // Changed from ordersImageId to imageUrl
            "timestamp" to FieldValue.serverTimestamp(),
            "paymentMethod" to paymentMethod
        )
        orderCollection.add(orderData)
            .addOnSuccessListener { documentReference ->
                Log.d("OrderViewModel", "Order added to Firestore with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("OrderViewModel", "Error adding order to Firestore", e)
            }
    }

    private fun removeFirestoreOrder(userId: String, order: MyOrders) {
        val orderCollection = firestore.collection("orders")
        orderCollection.whereEqualTo("userId", userId)
            .whereEqualTo("name", order.name)
            .whereEqualTo("price", order.price)
            .whereEqualTo("imageUrl", order.imageUrl) // Changed from ordersImageId to imageUrl
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            Log.d("OrderViewModel", "Order removed from Firestore")
                        }
                        .addOnFailureListener { e ->
                            Log.e("OrderViewModel", "Error removing order from Firestore", e)
                        }
                }
            }
    }

    private fun addFirestoreFavorite(userId: String, order: MyOrders) {
        val favoritesCollection = firestore.collection("favorites")
        val favoriteData = hashMapOf(
            "userId" to userId,
            "name" to order.name,
            "price" to order.price,
            "quantity" to order.quantity,
            "imageUrl" to order.imageUrl, // Changed from ordersImageId to imageUrl
            "timestamp" to FieldValue.serverTimestamp()
        )
        favoritesCollection.add(favoriteData)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    "OrderViewModel",
                    "Favorite added to Firestore with ID: ${documentReference.id}"
                )
            }
            .addOnFailureListener { e ->
                Log.w("OrderViewModel", "Error adding favorite to Firestore", e)
            }
    }

    private fun removeFirestoreFavorite(userId: String, order: MyOrders) {
        val favoritesCollection = firestore.collection("favorites")
        favoritesCollection.whereEqualTo("userId", userId)
            .whereEqualTo("name", order.name)
            .whereEqualTo("price", order.price)
            .whereEqualTo("imageUrl", order.imageUrl) // Changed from ordersImageId to imageUrl
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            Log.d("OrderViewModel", "Favorite removed from Firestore")
                        }
                        .addOnFailureListener { e ->
                            Log.e("OrderViewModel", "Error removing favorite from Firestore", e)
                        }
                }
            }
    }

    private fun updateFirestoreOrder(userId: String, order: MyOrders) {
        val orderCollection = firestore.collection("orders")
        orderCollection.whereEqualTo("userId", userId)
            .whereEqualTo("name", order.name)
            .whereEqualTo("price", order.price)
            .whereEqualTo("imageUrl", order.imageUrl) // Changed from ordersImageId to imageUrl
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    document.reference.update("quantity", order.quantity)
                        .addOnSuccessListener {
                            Log.d("OrderViewModel", "Order updated in Firestore")
                        }
                        .addOnFailureListener { e ->
                            Log.e("OrderViewModel", "Error updating order in Firestore", e)
                        }
                }
            }
    }

    fun clearOrders() {
        _orders.value = emptyList()
        shouldLoadOrders = false // Prevent loading orders after clearing
    }

    private fun clearFirestoreOrders(userId: String) {
        val orderCollection = firestore.collection("orders")
        orderCollection.whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            Log.d("OrderViewModel", "Order cleared from Firestore")
                        }
                        .addOnFailureListener { e ->
                            Log.e("OrderViewModel", "Error clearing order from Firestore", e)
                        }
                }
            }
            .addOnFailureListener { exception ->
                Log.e(
                    "OrderViewModel",
                    "Error getting orders for clearing from Firestore",
                    exception
                )
            }
    }

    fun loadOrders(userId: String) {
        if (!shouldLoadOrders) {
            Log.d("OrderViewModel", "Loading orders skipped as shouldLoadOrders is false")
            return
        }
        val orderCollection = firestore.collection("orders")
        orderCollection.whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val ordersList = mutableListOf<MyOrders>()
                for (document in result) {
                    val order = document.toObject<MyOrders>()
                    ordersList.add(order)
                }
                _orders.value = ordersList
                Log.d("OrderViewModel", "Orders loaded: $ordersList")
            }
            .addOnFailureListener { exception ->
                Log.e("OrderViewModel", "Error getting orders for user $userId", exception)
                _orders.value = emptyList() // Clear previous data or handle error state
            }
    }

    private fun loadFavorites(userId: String) {
        val favoritesCollection = firestore.collection("favorites")
        favoritesCollection.whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val favoritesList = mutableListOf<MyOrders>()
                for (document in result) {
                    val order = document.toObject<MyOrders>()
                    favoritesList.add(order)
                }
                _favorites.value = favoritesList
            }
            .addOnFailureListener { exception ->
                Log.e("OrderViewModel", "Error getting favorites for user $userId", exception)
                _favorites.value = emptyList() // Clear previous data or handle error state
            }
    }

    fun isFavorite(order: MyOrders): Boolean {
        return _favorites.value?.any {
            it.name == order.name &&
                    it.price == order.price &&
                    it.imageUrl == order.imageUrl
        } == true
    }

    private fun updatePaymentMethod(
        userId: String,
        paymentMethod: String,
        callback: (Boolean) -> Unit
    ) {
        val orderCollection = firestore.collection("orders")
        orderCollection.whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->
                val batch = firestore.batch()
                for (document in result) {
                    batch.update(document.reference, "paymentMethod", paymentMethod)
                }
                batch.commit()
                    .addOnSuccessListener {
                        Log.d("OrderViewModel", "Payment method updated in Firestore")
                        callback(true)
                    }
                    .addOnFailureListener { e ->
                        Log.e("OrderViewModel", "Error updating payment method in Firestore", e)
                        callback(false)
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("OrderViewModel", "Error getting orders to update payment method", exception)
                callback(false)
            }
    }

    private fun logFavoriteEvent(event: String, order: MyOrders) {
        val nameString = order.name.joinToString(", ")
        val bundle = Bundle().apply {
            putString("order_name", nameString)
            putDouble("order_price", order.price)
            putInt("order_quantity", order.quantity)
            putString(
                "order_image_url",
                order.imageUrl
            ) // Changed from order_image_id to order_image_url
        }
        firebaseAnalytics.logEvent(event, bundle)
    }

    private fun fetchPopularItems() {
        viewModelScope.launch {
            firestore.collection("products").whereEqualTo("type", "popular")
                .get()
                .addOnSuccessListener { documents ->
                    val items = documents.mapNotNull { document ->
                        document.toObject<Product>()
                    }
                    _popularItems.value = items
                }
                .addOnFailureListener { e ->
                    Log.e("OrderViewModel", "Error fetching popular items", e)
                }
        }
    }

    private fun fetchOfferItems() {
        viewModelScope.launch {
            firestore.collection("products").whereEqualTo("type", "offer")
                .get()
                .addOnSuccessListener { documents ->
                    val items = documents.mapNotNull { document ->
                        document.toObject<Product>()
                    }
                    _offerItems.value = items
                }
                .addOnFailureListener { e ->
                    Log.e("OrderViewModel", "Error fetching offer items", e)
                }
        }
    }

    private fun fetchRecommendations() {
        viewModelScope.launch {
            firestore.collection("products").whereEqualTo("type", "recommendation")
                .get()
                .addOnSuccessListener { documents ->
                    val items = documents.mapNotNull { document ->
                        document.toObject<Product>()
                    }
                    _recommendations.value = items
                }
                .addOnFailureListener { e ->
                    Log.e("OrderViewModel", "Error fetching recommendations", e)
                }
        }
    }


    private fun updateProductCount(orderList: List<MyOrders>) {
        val batch = firestore.batch()

        // Create a list of tasks to retrieve the current counts
        val tasks = orderList.map { order ->
            val countRef = firestore.collection("count").document(order.name.joinToString(", "))
            countRef.get()
        }

        // Wait for all get tasks to complete
        Tasks.whenAllComplete(tasks).addOnSuccessListener { taskResults ->
            taskResults.forEachIndexed { index, task ->
                if (task.isSuccessful) {
                    val document = (task.result as DocumentSnapshot)
                    val order = orderList[index]
                    val countRef =
                        firestore.collection("count").document(order.name.joinToString(", "))

                    if (document.exists()) {
                        val currentCount = document.getLong("count") ?: 0
                        batch.update(countRef, "count", currentCount + order.quantity)
                    } else {
                        batch.set(countRef, mapOf("count" to order.quantity))
                    }
                } else {
                    Log.e("OrderViewModel", "Error getting document: ${task.exception}")
                }
            }

            // Commit the batch
            batch.commit().addOnSuccessListener {
                Log.d("OrderViewModel", "Product counts updated successfully")
            }.addOnFailureListener { e ->
                Log.e("OrderViewModel", "Error updating product counts", e)
            }
        }.addOnFailureListener { e ->
            Log.e("OrderViewModel", "Error getting documents for update", e)
        }
    }

    fun isInCart(order: MyOrders): Boolean {
        return _orders.value?.any {
            it.name == order.name &&
                    it.price == order.price &&
                    it.imageUrl == order.imageUrl
        } == true
    }

    fun checkCouponCode(couponCode: String, callback: (Boolean, Double) -> Unit) {
        val db = Firebase.firestore

        db.collection("coupons")
            .whereEqualTo("coupon", couponCode)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val discountValue = when (val discount = document.get("discount")) {
                        is Number -> discount.toDouble()
                        is String -> discount.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                    callback(true, discountValue)
                } else {
                    callback(false, 0.0)
                }
            }
            .addOnFailureListener {
                callback(false, 0.0)
            }
    }
    fun getTotalAmount(): Double {
        val baseAmount = _orders.value?.sumOf { it.price * it.quantity } ?: 0.0
        val totalAmount = (baseAmount * 1.2) + 2.25
        updateGrandTotal(totalAmount) // Update the grand total
        return totalAmount
    }

    fun updateGrandTotal(total: Double) {
        _grandTotal.value = total
    }
    fun calculateAndUpdateGrandTotal() {
        val baseAmount = orders.value?.sumOf { it.price * it.quantity } ?: 0.0
        val totalAmount = (baseAmount * 1.2) + 2.25
        updateGrandTotal(totalAmount)
    }

}

