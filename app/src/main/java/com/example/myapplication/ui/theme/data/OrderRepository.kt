

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class OrderRepository(val firestore: FirebaseFirestore) {

    suspend fun getOrdersForUser(userId: String): List<MyOrders> {
        return withContext(Dispatchers.IO) {
            val ordersSnapshot = firestore.collection("orders").whereEqualTo("userId", userId).get().await()
            val orders = mutableListOf<MyOrders>()
            for (orderDocument in ordersSnapshot.documents) {
                val order = orderDocument.toObject(MyOrders::class.java)
                order?.let { orders.add(it) }
            }
            orders
        }
    }
}
