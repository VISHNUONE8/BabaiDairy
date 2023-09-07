package com.andrayudu.sureshdiaryfoods.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.model.OrderModel

class OrderDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_details)



        val orderModel = intent.getParcelableExtra<OrderModel>("orderModel")
        Log.i("TAG",""+orderModel.toString())

    }
}