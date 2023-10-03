package com.andrayudu.sureshdiaryfoods.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.ActivityOrderDetailsBinding
import com.andrayudu.sureshdiaryfoods.model.OrderModel

//this activity displays order details to only the customer...
class OrderDetails : AppCompatActivity() {

    private val tag = "OrderDetails"

    private lateinit var binding:ActivityOrderDetailsBinding

    //UI components
    private lateinit var actionBarBackButton: ImageView
    private lateinit var actionBarTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_order_details)


        initViews()
        initClickListeners()

        val orderModel = intent.getParcelableExtra<OrderModel>("orderModel")
        setOrderDetails(orderModel)

    }

    private fun initClickListeners() {
        actionBarBackButton.setOnClickListener {
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    finish()
                }
            })
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun initViews() {
        actionBarBackButton = binding.actionbarOrderDetails.findViewById(R.id.actionbar_Back)
        actionBarTextView = binding.actionbarOrderDetails.findViewById(R.id.actionbar_Text)
        actionBarTextView.text = "OrderDetails"
    }

    //based on orderStatus it will name it accordingly..
    private fun getOrderStatus(orderStatus: String?): String? {
        when(orderStatus){
            "-1"-> {
                binding.statusTV.setTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                return "Pending"
            }

            "0"->{
                binding.statusTV.setTextColor(ContextCompat.getColor(this,R.color.colorOrange))
                return "Placed"
            }

            "1"->{
                binding.statusTV.setTextColor(ContextCompat.getColor(this,R.color.colorGreen))
                return "Dispatched"
            }
        }
        return orderStatus
    }


    private fun setOrderDetails(orderModel: OrderModel?) {
        if (orderModel!=null){

            val orderStatus = (orderModel.orderStatus)
            val orderStatusStr = getOrderStatus(orderStatus)

            //when the order is dispatched via a transport it will have transport company and lr...
            if (orderStatus == "1"){
                binding.transportNameDisplay.text = orderModel.transportCompany
                binding.lrNoDisplay.text = orderModel.transportLrNo
            }
            binding.dateTV.append(orderModel.date)
            binding.orderIdTV.append(orderModel.orderId)
            binding.amountTV.append("${orderModel.orderValue}/-")
            binding.statusTV.append(orderStatusStr)
            binding.itemsDisplay.setMovementMethod(ScrollingMovementMethod())
            for (item in orderModel.cartItemList!!){
                binding.itemsDisplay.append("${item.Name} * ${item.Quantity}\n")
            }
        }

    }
}