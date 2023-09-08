package com.andrayudu.sureshdiaryfoods.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.CustomerOrdersItemBinding
import com.andrayudu.sureshdiaryfoods.databinding.OrdersItemBinding
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.model.OrderModel

class OrdersAdapter( private val clickListener: (OrderModel?)->Unit) : RecyclerView.Adapter<OrdersViewHolder>(){


    private var customerOrdersList1 = ArrayList<OrderModel>()



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        val layoutInflater
                = LayoutInflater.from(parent.context)
        val binding : CustomerOrdersItemBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.customer_orders_item,parent,false)
        return OrdersViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return customerOrdersList1.size

    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        holder.bind(customerOrdersList1[position],clickListener)
    }

    fun setList(
        customerOrdersList: ArrayList<OrderModel>
    ){
        customerOrdersList1.clear()
        customerOrdersList1.addAll(customerOrdersList)
        notifyDataSetChanged()

    }
}
class OrdersViewHolder(val binding: CustomerOrdersItemBinding): RecyclerView.ViewHolder(binding.root){
    fun bind(
        orderModel: OrderModel,
        clickListener: (OrderModel?) -> Unit
    ){





       binding.dateTV.text = orderModel.date
        binding.orderIdTV.text = orderModel.orderId
        binding.OrderValueTV.text = orderModel.orderValue
        binding.quantityTV.text = orderModel.quantity







        binding.cardview.setOnClickListener{
            clickListener(orderModel)
        }




    }
}
