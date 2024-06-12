package com.andrayudu.sureshdiaryfoods.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.CustomerOrdersItemBinding
import com.andrayudu.sureshdiaryfoods.model.OrderModel

class OrdersRVAdapter(private val clickListener: (OrderModel?)->Unit) : RecyclerView.Adapter<OrdersViewHolder>(){



    val tag = "OrdersAdapter"

    private val customerOrdersList1 = ArrayList<OrderModel>()
    private val datesList1 = ArrayList<Int>()



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
        if (datesList[position] == 0){
            binding.dateTV.visibility = View.GONE
        }
        else{
            binding.dateTV.visibility = View.VISIBLE
            binding.dateTV.text = orderModel.date
        }

        val orderStatusStr = getOrderStatus(orderModel.orderStatus)
        //kdslfjlkasf
        val cartItems = orderModel.cartItemList
        val firstItemName = cartItems?.get(0)?.Name
        val firstItemQuantity = cartItems?.get(0)?.Quantity
        val itemNames = "Items: $firstItemName * $firstItemQuantity ...."
        val orderId = "OrderId:${orderModel.orderId}"
        var orderValue = "Amount:${orderModel.grandTotal}"
        val orderStatus = "Status:${orderStatusStr}"
        //if the order is dispatched then,dispatched grand total should be shown as price
        if(orderModel.orderStatus == 1){
            orderValue = "Amount:${orderModel.dispatchedGrandTotal}"
        }



        binding.itemNameTV.text = itemNames
        binding.orderIdTV.text = orderId
        binding.OrderValueTV.text = orderValue
        binding.orderStatus.text = orderStatus

        binding.cardview.setOnClickListener{
            clickListener(orderModel)
        }


    }

    private fun getOrderStatus(orderStatus: Int): String? {
        when(orderStatus){
            -1-> {
                binding.orderStatus.setTextColor(Color.parseColor("#F44336"))
                return "Pending"
            }

            0->{
                binding.orderStatus.setTextColor(Color.parseColor("#FFA500"))
                return "Placed"
            }

            1->{
                binding.orderStatus.setTextColor(Color.parseColor("#0F9D58"))
                return "Dispatched"
            }
        }
        return "orderStatus"
    }
}
