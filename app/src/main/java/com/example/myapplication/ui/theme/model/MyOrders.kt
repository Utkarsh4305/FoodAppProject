import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class MyOrders(
    val name: List<String> = emptyList(),
    val price: Double = 0.0,
    val imageUrl: String = "",
    val quantity: Int = 1
) : Parcelable {
    // No-argument constructor for Firestore
    constructor() : this(emptyList(), 0.0, "", 1)
}
