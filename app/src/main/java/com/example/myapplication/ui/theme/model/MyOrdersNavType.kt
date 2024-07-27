package com.example.myapplication.ui.theme.model

import MyOrders
import android.os.Bundle
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.google.gson.Gson

class MyOrdersNavType : NavType<MyOrders>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): MyOrders? {
        return bundle.getParcelable(key)
    }

    override fun parseValue(value: String): MyOrders {
        return Gson().fromJson(value, MyOrders::class.java)
    }

    override fun put(bundle: Bundle, key: String, value: MyOrders) {
        bundle.putParcelable(key, value)
    }
}
