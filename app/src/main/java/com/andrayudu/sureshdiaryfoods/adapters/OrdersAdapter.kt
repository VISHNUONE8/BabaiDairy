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


    val tag = "OrdersAdapter"
    private var customerOrdersList1 = ArrayList<OrderModel>()
    private var datesList1 = ArrayList<Int>()



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        val layoutInflater
                = LayoutInflater.from(parent.context)
        val binding : CustomerOrdersItemBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.customer_orders_item,parent,false)
        return OrdersViewHolder(binding,datesList1)
    }

    override fun getItemCount(): Int {
        return customerOrdersList1.size

    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        holder.bind(customerOrdersList1[position],clickListener,position)
    }

    fun setList(
        customerOrdersList: List<OrderModel>,
        datesList:ArrayList<Int>
    ){
        customerOrdersList1.clear()
        datesList1.clear()
        customerOrdersList1.addAll(customerOrdersList)
        datesList1.addAll(datesList)
        Log.i(tag,"the dates list values are:"+datesList1.toString())
        notifyDataSetChanged()

    }
}
class OrdersViewHolder(val binding: CustomerOrdersItemBinding,val datesList: ArrayList<Int>): RecyclerView.ViewHolder(binding.root){
    fun bind(
        orderModel: OrderModel,
        clickListener: (OrderModel?) -> Unit,
        position: Int
    ){

        //if the orders date is same as above ones then we will hide the date display TV
        if (datesList.get(position).equals(0)){
            binding.dateTV.visibility = View.GONE
        }
        else{
            binding.dateTV.visibility = View.VISIBLE
            binding.dateTV.text = orderModel.date
        }

        val cartItems = orderModel.cartItemList
        val firstItemName = cartItems?.get(0)?.Name
        val firstItemQuantity = cartItems?.get(0)?.Quantity
        binding.itemNameTV.text = "Items: $firstItemName * $firstItemQuantity ...."
        binding.orderIdTV.text = "OrderId:${orderModel.orderId}"
        binding.OrderValueTV.text = "Amount:${orderModel.orderValue}"
        binding.orderStatus.text = "Status:${orderModel.orderStatus}"

        binding.cardview.setOnClickListener{
            clickListener(orderModel)
        }




    }
}
