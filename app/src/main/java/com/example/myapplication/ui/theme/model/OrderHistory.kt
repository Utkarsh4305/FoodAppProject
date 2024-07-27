package com.example.myapplication.ui.theme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue
import java.sql.Timestamp
import java.util.Date

@Parcelize
data class OrderHistory(
    val documentId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val totalAmount: Double = 0.0,
    val items: @RawValue List<Map<String, Any>> = emptyList(),
    val timestamp: Timestamp = Timestamp(Date().time),
    var feedback: String = "",
    var feedbackGiven: Boolean = false
) : Parcelable {
    // No-argument constructor for Firestore
    constructor() : this("","", 0.0, 0, 0.0,
        emptyList(), Timestamp(Date().time), "", false)
}
