package com.andrayudu.sureshdiaryfoods.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.ActivityOrderDetailsBinding
import com.andrayudu.sureshdiaryfoods.model.OrderModel

class OrderDetails : AppCompatActivity() {

    private lateinit var actionBarBackButton: ImageView
    private lateinit var actionBarTextView: TextView
    private lateinit var binding:ActivityOrderDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = DataBindingUtil.setContentView(this,R.layout.activity_order_details)
        actionBarBackButton = binding.actionbarOrderDetails.findViewById(R.id.actionbar_Back)
        actionBarTextView = binding.actionbarOrderDetails.findViewById(R.id.actionbar_Text)
        actionBarTextView.text = "OrderDetails"

        actionBarBackButton.setOnClickListener {
            onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
                override fun handleOnBackPressed() {

                    finish()
                }
            })
            onBackPressedDispatcher.onBackPressed()
        }

        val orderModel = intent.getParcelableExtra<OrderModel>("orderModel")
        setOrderDetails(orderModel)

    }

    private fun setOrderDetails(orderModel: OrderModel?) {
        if (orderModel!=null){
            binding.dateTV.append(orderModel.date)
            binding.orderIdTV.append(orderModel.orderId)
            binding.amountTV.append("${orderModel.orderValue}/-")
            binding.statusTV.append(orderModel.orderStatus)
            for (item in orderModel.cartItemList!!){
                binding.itemsDisplay.append("${item.Name} * ${item.Quantity}\n")
            }
        }



    }
}