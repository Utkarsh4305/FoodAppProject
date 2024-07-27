import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.myapplication.R
import com.example.myapplication.ui.theme.model.OrderHistory
import com.example.myapplication.ui.theme.theme.Purple200

@Composable
fun OrderHistoryScreen(
    navController: NavHostController,
    orderHistoryViewModel: OrderHistoryViewModel
) {
    val orders by orderHistoryViewModel.orders.observeAsState(initial = null)

    LaunchedEffect(Unit) {
        orderHistoryViewModel.loadOrders()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFF0F0F0)),  // Light background color
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Order History", style = MaterialTheme.typography.h4.copy(fontSize = 24.sp, fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(16.dp))  // Space between title and list
        orders?.let { orderList ->
            if (orderList.isEmpty()) {
                Text("No orders found", style = MaterialTheme.typography.body1.copy(fontSize = 16.sp))
            } else {
                val sortedOrderList = orderList.sortedByDescending { it.timestamp }
                LazyColumn {
                    items(sortedOrderList) { order ->
                        OrderCard(order) { feedback ->
                            orderHistoryViewModel.submitFeedback(order, feedback)
                        }
                    }
                }
            }
        } ?: run {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun OrderCard(order: OrderHistory, onSubmitFeedback: (String) -> Unit) {
    var feedback by remember { mutableStateOf(order.feedback) }
    var rating by remember { mutableStateOf(order.feedback.toIntOrNull() ?: 0) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = 6.dp,
        backgroundColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Order Items:", style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
            order.items.forEach { item ->
                Text("Item Name: ${item["name"]}", style = MaterialTheme.typography.body1.copy(fontSize = 16.sp))
                Text("Price: $${item["price"]}", style = MaterialTheme.typography.body2.copy(fontSize = 14.sp, color = Color.Gray))
                Text("Quantity: ${item["quantity"]}", style = MaterialTheme.typography.body2.copy(fontSize = 14.sp, color = Color.Gray))
                Spacer(modifier = Modifier.height(8.dp))
            }
            Divider(color = Color.LightGray, thickness = 1.dp)  // Divider between order items and total amount
            Spacer(modifier = Modifier.height(8.dp))
            Text("Total Amount: $${order.totalAmount}", style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold))
            Text("TimeStamp: ${order.timestamp}", style = MaterialTheme.typography.body2.copy(fontSize = 14.sp, color = Color.Gray))
            Spacer(modifier = Modifier.height(8.dp))

            if (!order.feedbackGiven) {
                RatingBar(rating = rating) {
                    rating = it
                }
                Button(
                    onClick = {
                        feedback = rating.toString()
                        onSubmitFeedback(feedback)
                    },
                    modifier = Modifier.align(Alignment.End),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF6200EE))
                ) {
                    Text("Submit Feedback", color = Color.White)
                }
            } else {
                Text("Feedback: ${order.feedback}", style = MaterialTheme.typography.body1.copy(fontSize = 16.sp))
            }
        }
    }
}

@Composable
fun RatingBar(rating: Int, onRatingChanged: (Int) -> Unit) {
    Row(modifier = Modifier.padding(8.dp)) {
        for (i in 1..5) {
            Icon(
                painter = painterResource(
                    id = if (i <= rating) R.drawable.baseline_star_24 else R.drawable.baseline_star_outline_24
                ),
                contentDescription = "Star $i",
                modifier = Modifier
                    .size(32.dp)
                    .clickable { onRatingChanged(i) },
                tint = if (i <= rating) Color.Yellow else Color.Gray
            )
        }
    }
}
