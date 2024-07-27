import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.theme.model.OrderHistory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.sql.Timestamp

class OrderHistoryViewModel(
    private val repository: OrderRepository,
    val userId: String
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _orders = MutableLiveData<List<OrderHistory>?>(null)
    val orders: LiveData<List<OrderHistory>?> get() = _orders

    init {
        loadOrders()
    }

    fun loadOrders() {
        viewModelScope.launch {
            firestore.collection("confirmedOrders")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { result ->
                    val ordersList = result.map { document ->
                        val items = document.get("items") as? List<Map<String, Any>> ?: emptyList()
                        val name = if (items.isNotEmpty()) items[0]["name"] as? String ?: "" else ""
                        val price = document.getDouble("price") ?: 0.0
                        val quantity = document.getLong("quantity")?.toInt() ?: 0
                        val totalAmount = document.getDouble("totalAmount") ?: 0.0
                        val feedback = document.getString("feedback") ?: ""
                        val feedbackGiven = document.getBoolean("feedbackGiven") ?: false

                        OrderHistory(
                            document.id,
                            name,
                            price,
                            quantity,
                            totalAmount,
                            items,
                            Timestamp(document.getDate("timestamp")?.time ?: 0L),
                            feedback,
                            feedbackGiven
                        )
                    }
                    _orders.value = ordersList
                }
                .addOnFailureListener { exception ->
                    // Handle failure
                }
        }
    }

    fun submitFeedback(order: OrderHistory, feedback: String) {
        val updatedOrder = order.copy(feedback = feedback, feedbackGiven = true)
        // Update the local list
        val updatedOrders = _orders.value?.map {
            if (it.documentId == order.documentId) updatedOrder else it
        }
        _orders.value = updatedOrders

        // Update Firestore
        firestore.collection("confirmedOrders")
            .document(order.documentId)
            .update("feedback", feedback, "feedbackGiven", true)
    }

    class OrderHistoryViewModelFactory(
    private val repository: OrderRepository,
    private val userId: String
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderHistoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OrderHistoryViewModel(repository, userId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
    }
}
