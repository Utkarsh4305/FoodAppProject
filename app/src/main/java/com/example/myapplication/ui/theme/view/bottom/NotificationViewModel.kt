package com.example.myapplication.ui.theme.view.bottom

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

class NotificationViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _notifications = MutableLiveData<List<Notification>>()
    val notifications: LiveData<List<Notification>> get() = _notifications

    private var notificationListenerRegistration: ListenerRegistration? = null

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        val user = auth.currentUser ?: return
        notificationListenerRegistration?.remove() // Remove any existing listener

        notificationListenerRegistration = firestore.collection("confirmedOrders")
            .whereEqualTo("userId", user.uid)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                val notificationsList = mutableListOf<Notification>()
                for (doc in snapshots!!) {
                    val orderId = doc.id
                    val timestamp = doc.getTimestamp("timestamp")?.toDate() ?: continue
                    notificationsList.add(Notification(orderId, timestamp))
                }

                // Sort notifications in descending order of timestamp
                notificationsList.sortByDescending { it.timestamp }

                _notifications.value = notificationsList
            }
    }

    override fun onCleared() {
        super.onCleared()
        notificationListenerRegistration?.remove()
    }
}

data class Notification(
    val orderId: String,
    val timestamp: Date
) {
    val formattedDate: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(timestamp)

    override fun toString(): String {
        return "Order confirmed with ID: $orderId on $formattedDate"
    }
}

class NotificationViewModelFactory(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationViewModel(auth, firestore) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}