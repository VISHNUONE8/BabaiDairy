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

    private var foodItemsList = ArrayList<CartItem>()
    private var datesList1 = ArrayList<String>()
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
        holder.bind(datesList1,customerOrdersList1[position],clickListener)
    }

    fun setList(
        cartItems: List<CartItem>,
        datesList: ArrayList<String>,
        customerOrdersList: ArrayList<OrderModel>
    ){
        foodItemsList.clear()
        datesList1.clear()
        customerOrdersList1.clear()
        foodItemsList.addAll(cartItems)
        datesList1.addAll(datesList)
        customerOrdersList1.addAll(customerOrdersList)
        Log.i("the dates lisst is:",""+datesList1.toString())
        notifyDataSetChanged()

    }
}
class OrdersViewHolder(val binding: CustomerOrdersItemBinding): RecyclerView.ViewHolder(binding.root){
    fun bind(
        datesList: ArrayList<String>,
        orderModel: OrderModel,
        clickListener: (OrderModel?) -> Unit
    ){

//        if (datesList.isNotEmpty()) {
//            val date = datesList.get(adapterPosition)
//            binding.year.text = date
//            if(datesList.get(adapterPosition).isEmpty()){
//                binding.year.visibility = View.GONE
//            }
//        }



       binding.dateTV.text = orderModel.date
        binding.orderIdTV.text = orderModel.orderId
        binding.OrderValueTV.text = orderModel.orderValue
        binding.quantityTV.text = orderModel.quantity







        binding.cardview.setOnClickListener{
            clickListener(orderModel)
        }




    }
}
