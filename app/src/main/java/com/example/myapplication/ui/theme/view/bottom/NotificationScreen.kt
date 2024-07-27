    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.items
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material.Card
    import androidx.compose.material.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.getValue
    import androidx.compose.runtime.livedata.observeAsState
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.unit.dp
    import androidx.lifecycle.viewmodel.compose.viewModel
    import androidx.navigation.NavHostController
    import com.example.myapplication.ui.theme.view.bottom.NotificationViewModel

    @Composable
    fun NotificationScreen(
        navController: NavHostController,
        notificationViewModel: NotificationViewModel = viewModel()
    ) {
        val notifications by notificationViewModel.notifications.observeAsState(emptyList())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            items(notifications) { notification ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    elevation = 4.dp,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text(
                        text = notification.toString(),  // This will use the overridden toString() method
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
