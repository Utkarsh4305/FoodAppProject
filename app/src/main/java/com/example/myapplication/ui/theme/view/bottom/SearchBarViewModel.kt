import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.ui.theme.model.Uploading
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SearchBarViewModel : ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText: StateFlow<String> = _searchText

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching

    private val _products = MutableStateFlow<List<Uploading>>(emptyList())
    val products: StateFlow<List<Uploading>> = _products

    init {
        fetchAllProducts()
    }

    fun onSearchTextChange(newText: String) {
        _searchText.value = newText
    }

    private fun fetchAllProducts() {
        viewModelScope.launch {
            _isSearching.value = true
            val productsList = fetchAllProductDetails()
            _products.value = productsList
            _isSearching.value = false
        }
    }

    suspend fun fetchAllProductDetails(): List<Uploading> {
        val db = FirebaseFirestore.getInstance()
        val snapshot = db.collection("products").get().await()
        return snapshot.documents.mapNotNull { document ->
            val price = document.get("price").toString().toDoubleOrNull() ?: 0.0
            Uploading(
                description = document.getString("description") ?: "",
                imageUrl = document.getString("imageUrl") ?: "",
                name = document.getString("name") ?: "",
                price = price.toString(),
                category = document.getString("category") ?: ""
            )
        }
    }
}
