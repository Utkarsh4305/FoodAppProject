package com.example.myapplication.ui.theme.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Uploading(
    val category: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val name: String = "",
    val price: String = " ",
    val uploadername : String = ""
) : Parcelable {
    // No-argument constructor for Firestore
    constructor() : this("", "", "", "", "","")
}
