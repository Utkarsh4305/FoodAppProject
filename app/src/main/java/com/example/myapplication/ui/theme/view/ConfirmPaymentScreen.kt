import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.myapplication.ui.theme.navigation.Screen
import com.example.myapplication.ui.theme.theme.colorBlack
import com.example.myapplication.ui.theme.theme.colorRedDark
import com.example.myapplication.ui.theme.theme.colorWhite
import com.example.myapplication.ui.theme.view.OrderViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun PaymentScreen(navController: NavHostController, orderViewModel: OrderViewModel = viewModel()) {
    val context = LocalContext.current
    val orders by orderViewModel.orders.observeAsState(emptyList())
    val paymentOptions = listOf("Credit Card", "PayPal", "Google Pay", "Apple Pay")
    var selectedPaymentOption by remember { mutableStateOf(paymentOptions[0]) }
    var swipeButtonState by remember { mutableStateOf(SwipeButtonState.INITIAL) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Select Payment Method",
            style = MaterialTheme.typography.h5,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        paymentOptions.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { selectedPaymentOption = option }
                    .border(
                        width = 1.dp,
                        color = if (selectedPaymentOption == option) colorRedDark else Color.Gray,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedPaymentOption == option,
                    onClick = { selectedPaymentOption = option },
                    colors = RadioButtonDefaults.colors(selectedColor = colorRedDark)
                )
                Text(option, modifier = Modifier.padding(start = 8.dp))
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        ConfirmOrderSwipeButton(
            orderViewModel = orderViewModel,
            selectedPaymentOption = selectedPaymentOption,
            navController = navController,
            context = context,
            swipeButtonState = swipeButtonState,
            onSwipeComplete = { swipeButtonState = SwipeButtonState.COLLAPSED }
        )
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun ConfirmOrderSwipeButton(
    orderViewModel: OrderViewModel,
    selectedPaymentOption: String,
    navController: NavController,
    context: Context,
    swipeButtonState: SwipeButtonState,
    onSwipeComplete: () -> Unit
) {
    var currentState by remember { mutableStateOf(swipeButtonState) }
    val coroutineScope = rememberCoroutineScope()

    SwipeButton(
        onSwiped = {
            val orderList = orderViewModel.orders.value
            if (orderList.isNullOrEmpty()) {
                Toast.makeText(context, "No orders to confirm", Toast.LENGTH_SHORT).show()
            } else {
                currentState = SwipeButtonState.SWIPED
                coroutineScope.launch {
                    delay(2000) // Simulate loading time
                    orderViewModel.confirmOrder(selectedPaymentOption)
                    Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_SHORT).show()
                    currentState = SwipeButtonState.COLLAPSED
                    onSwipeComplete()
                    navController.navigate(Screen.HomeScreen.route) {
                        popUpTo(Screen.HomeScreen.route) { inclusive = true }
                    }
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp),
        swipeButtonState = currentState,
        colors = ButtonDefaults.buttonColors(backgroundColor = colorBlack),
        shape = RoundedCornerShape(24.dp),
        content = {
            Text(
                text = "Swipe to confirm",
                color = colorWhite,
                style = MaterialTheme.typography.button,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }
    )
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterialApi::class)
@Composable
fun SwipeButton(
    onSwiped: () -> Unit,
    modifier: Modifier = Modifier,
    swipeButtonState: SwipeButtonState,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = ButtonDefaults.elevation(),
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    icon: ImageVector = Icons.Default.ArrowForward,
    rotateIcon: Boolean = true,
    iconPadding: PaddingValues = PaddingValues(2.dp),
    content: @Composable RowScope.() -> Unit
) {
    val contentColor by colors.contentColor(enabled)
    val backgroundColor by colors.backgroundColor(enabled)
    val dragOffset = remember { mutableStateOf(0f) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        contentColor = contentColor.copy(alpha = 1f),
        border = border,
        elevation = elevation?.elevation(enabled, interactionSource)?.value ?: 0.dp
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart
        ) {
            val maxWidth = this.constraints.maxWidth.toFloat()

            when (swipeButtonState) {
                SwipeButtonState.COLLAPSED -> {
                    val animatedProgress = remember {
                        androidx.compose.animation.core.Animatable(
                            initialValue = 0f
                        )
                    }
                    LaunchedEffect(Unit) {
                        animatedProgress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(600)
                        )
                    }
                    IconButton(
                        onClick = { },
                        modifier = Modifier
                            .scale(animatedProgress.value)
                            .padding(iconPadding)
                            .clip(CircleShape)
                            .background(MaterialTheme.colors.onPrimary)
                            .align(Alignment.Center)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Done,
                            contentDescription = "Done",
                            tint = MaterialTheme.colors.primary
                        )
                    }
                }
                SwipeButtonState.SWIPED -> {
                    HorizontalDottedProgressBar()
                }
                SwipeButtonState.INITIAL -> {
                    dragOffset.value = 0f // when button goes to initial state
                    CompositionLocalProvider(LocalContentAlpha provides contentColor.alpha) {
                        ProvideTextStyle(
                            value = MaterialTheme.typography.body2
                        ) {
                            Row(
                                Modifier
                                    .fillMaxSize()
                                    .padding(contentPadding),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically,
                                content = content
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = swipeButtonState != SwipeButtonState.SWIPED) {
                IconButton(
                    onClick = { },
                    enabled = enabled,
                    modifier = Modifier
                        .padding(iconPadding)
                        .align(Alignment.CenterStart)
                        .offset { IntOffset(dragOffset.value.roundToInt(), 0) }
                        .draggable(
                            enabled = enabled,
                            orientation = Orientation.Horizontal,
                            state = rememberDraggableState { delta ->
                                val newValue = dragOffset.value + delta
                                dragOffset.value = newValue.coerceIn(0f, maxWidth)
                            },
                            onDragStopped = {
                                if (dragOffset.value > maxWidth * 2 / 3) {
                                    dragOffset.value = maxWidth
                                    scope.launch {
                                        onSwiped()
                                    }
                                } else {
                                    dragOffset.value = 0f
                                }
                            }
                        )
                        .background(MaterialTheme.colors.onPrimary, shape = CircleShape)
                ) {
                    Icon(
                        imageVector = icon,
                        modifier = Modifier.graphicsLayer {
                            if (rotateIcon) {
                                rotationZ += dragOffset.value / 5
                            }
                        },
                        contentDescription = "Arrow",
                        tint = LocalContentColor.current,
                    )
                }
            }
        }
    }
}

@Composable
fun HorizontalDottedProgressBar() {
    val transition = rememberInfiniteTransition()
    val scale by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(300),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(3) { index ->
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .scale(scale)
                    .background(colorWhite, CircleShape)
                    .padding(4.dp)
            )
            if (index != 2) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

enum class SwipeButtonState {
    INITIAL, SWIPED, COLLAPSED
}