package com.example.myapplication.ui.theme.view

import MyOrders
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.example.myapplication.ui.theme.component.TopAppBarMyOrders
import com.example.myapplication.ui.theme.navigation.Screen
import com.example.myapplication.ui.theme.theme.colorBlack
import com.example.myapplication.ui.theme.theme.colorPurple400
import com.example.myapplication.ui.theme.theme.colorRedDark
import com.example.myapplication.ui.theme.theme.colorRedLite
import com.example.myapplication.ui.theme.theme.colorWhite
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.gson.Gson
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun OrderScreen(navController: NavHostController, orderViewModel: OrderViewModel = viewModel()) {
    var isLoading by remember { mutableStateOf(true) }

    Scaffold(
        topBar = { TopAppBarMyOrders(navController) },
        backgroundColor = if (isSystemInDarkTheme()) Color.Black else colorWhite,
        content = {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                OrderMainContent(orderViewModel, navController)
            }
        }
    )

    LaunchedEffect(Unit) {
        val user = orderViewModel.auth.currentUser
        user?.let {
            orderViewModel.loadOrders(it.uid)
            isLoading = false
        }
    }
}

@Composable
fun OrderMainContent(orderViewModel: OrderViewModel, navController: NavHostController) {
    Column(modifier = Modifier.fillMaxSize()) {
        OrderList(orderViewModel, navController)
        OrderCalculateData(orderViewModel, navController)
    }
}


@Composable
fun OrderCalculateData(orderViewModel: OrderViewModel, navController: NavHostController) {
    val orders by orderViewModel.orders.observeAsState(emptyList())
    var showCouponDialog by remember { mutableStateOf(false) }
    var couponCode by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf(0.0) }
    var showToast by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val grandTotal by orderViewModel.grandTotal.observeAsState(0.0)

    LaunchedEffect(orders, discount) {
        orderViewModel.updateGrandTotal(orders.sumOf { it.price * it.quantity } + 2.25 + (orders.sumOf { it.price * it.quantity } * 0.2) - discount)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Button(
            onClick = { showCouponDialog = true },
            colors = ButtonDefaults.buttonColors(backgroundColor = colorRedLite),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = "Apply Coupon",
                color = colorWhite,
                style = MaterialTheme.typography.button
            )
        }

        Spacer(modifier = Modifier.height(15.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(15.dp))

        PriceRow("Item total", "$${"%.2f".format(orders.sumOf { it.price * it.quantity })}")
        Spacer(modifier = Modifier.height(15.dp))
        PriceRow("Delivery fees", "$2.25")
        Spacer(modifier = Modifier.height(15.dp))
        PriceRow("Tax", "$${"%.2f".format(orders.sumOf { it.price * it.quantity } * 0.2)}")
        Spacer(modifier = Modifier.height(15.dp))

        if (discount > 0) {
            PriceRow("Discount", "-$${"%.2f".format(discount)}", color = Color.Green)
            Spacer(modifier = Modifier.height(15.dp))
        }

        PriceRow(
            "Total:",
            "$${"%.2f".format(grandTotal)}",
            textStyle = MaterialTheme.typography.h6,
            color = colorRedDark
        )

        Spacer(modifier = Modifier.height(15.dp))

        if (orders.isNotEmpty()) {
            Button(
                onClick = {
                    orderViewModel.updateGrandTotal(grandTotal) // Update the ViewModel with the new total
                    navController.navigate(Screen.ConfirmPaymentScreen.route)
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = colorBlack),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(
                    text = "Confirm order",
                    color = colorWhite,
                    style = MaterialTheme.typography.button
                )
            }
        }
    }

    if (showCouponDialog) {
        AlertDialog(
            onDismissRequest = { showCouponDialog = false },
            title = { Text(text = "Enter Coupon Code") },
            text = {
                TextField(
                    value = couponCode,
                    onValueChange = { couponCode = it },
                    label = { Text("Coupon Code") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            orderViewModel.checkCouponCode(couponCode) { isValid, discountValue ->
                                if (isValid) {
                                    discount = discountValue
                                    showToast = "Coupon applied!"
                                } else {
                                    showToast = "Invalid coupon code"
                                }
                                showCouponDialog = false
                            }
                        }
                    )
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        orderViewModel.checkCouponCode(couponCode) { isValid, discountValue ->
                            if (isValid) {
                                discount = discountValue
                                showToast = "Coupon applied!"
                            } else {
                                showToast = "Invalid coupon code"
                            }
                            showCouponDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = colorRedLite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                        .padding(start = 40.dp, end = 40.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                Button(
                    onClick = { showCouponDialog = false },
                    colors = ButtonDefaults.buttonColors(backgroundColor = colorRedLite),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp)
                        .padding(start = 40.dp, end = 40.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    LaunchedEffect(showToast) {
        showToast?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            showToast = null
        }
    }
}




@Composable
fun PriceRow(
    label: String,
    amount: String,
    textStyle: TextStyle = MaterialTheme.typography.button,
    color: Color = colorBlack
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color.Gray, style = textStyle)
        Text(text = amount, color = color, style = textStyle, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun OrderList(orderViewModel: OrderViewModel, navController: NavHostController) {
    val orders by orderViewModel.orders.observeAsState(emptyList())
    Spacer(modifier = Modifier.height(20.dp))
    Text(
        text = "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\tOrder Summary",
        style = MaterialTheme.typography.h5,
    )
    Spacer(modifier = Modifier.height(5.dp))
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .height(300.dp)
            .border(2.dp, colorRedLite, RoundedCornerShape(12.dp)),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(orders) { order ->
            MyOrdersListItem(order, orderViewModel, navController)
        }
    }
}

@Composable
fun MyOrdersListItem(myOrders: MyOrders, orderViewModel: OrderViewModel, navController: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            painter = rememberImagePainter(data = myOrders.imageUrl),
            contentDescription = null,
            modifier = Modifier
                .size(82.dp)
                .clip(RoundedCornerShape(8.dp))
        )
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .weight(1f)
                .padding(horizontal = 10.dp),
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = myOrders.name.joinToString(", "),
                style = MaterialTheme.typography.subtitle1,
                color = colorBlack,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$${"%.2f".format(myOrders.price * myOrders.quantity)}",
                style = MaterialTheme.typography.subtitle1,
                color = colorRedDark,
                fontWeight = FontWeight.Bold
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = {
                    if (myOrders.quantity > 1) {
                        orderViewModel.updateOrderQuantity(myOrders, myOrders.quantity - 1)
                    }
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colorRedLite)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrease quantity",
                    tint = colorWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = "${myOrders.quantity}",
                color = colorBlack,
                style = MaterialTheme.typography.subtitle1.copy(fontSize = 20.sp),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(
                onClick = {
                    orderViewModel.updateOrderQuantity(myOrders, myOrders.quantity + 1)
                },
                modifier = Modifier
                    .clip(CircleShape)
                    .background(colorRedLite)
                    .size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increase quantity",
                    tint = colorWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
            IconButton(
                onClick = { orderViewModel.removeOrder(myOrders) }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete order",
                    tint = colorRedLite
                )
            }
        }
    }
}


@Composable
fun HorizontalDivider() {
    Divider(
        color = colorRedLite, thickness = 1.dp,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp)
    )
}

@Composable
@Preview
fun OrderScreenPreview() {
    OrderScreen(navController = NavHostController(LocalContext.current))
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
fun OrderScreenDarkPreview() {
    OrderScreen(navController = NavHostController(LocalContext.current))
}

