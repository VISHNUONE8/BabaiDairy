package com.andrayudu.sureshdiaryfoods.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.layout.Layout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.CustomerOrdersItemBinding
import com.andrayudu.sureshdiaryfoods.databinding.UserOrdersAdminViewItemBinding
import com.andrayudu.sureshdiaryfoods.model.OrderModel

class UserOrdersAdminViewAdapter(private val clickListener: (OrderModel?) -> Unit):RecyclerView.Adapter<UserOrdersHolder>() {

    //in this,the customers whose orders undelivered(pending) should only come
    private var NamesList = ArrayList<OrderModel>()
    //since the default spinner position is 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserOrdersHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: UserOrdersAdminViewItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.user_orders_admin_view_item,parent,false)
        return UserOrdersHolder(binding)
    }

    override fun getItemCount(): Int {
        return NamesList.size
    }


    override fun onBindViewHolder(holder: UserOrdersHolder, position: Int) {
        holder.bind(NamesList[position],clickListener,position)
    }

    fun setList(
        customerNamesList:List<OrderModel>
    ){
        NamesList.clear()
        NamesList.addAll(customerNamesList)
        notifyDataSetChanged()
    }

    fun deleteItem(adapterPosition: Int) {

        NamesList.removeAt(adapterPosition)
        notifyItemRemoved(adapterPosition)
    }

}

class UserOrdersHolder(val binding: UserOrdersAdminViewItemBinding): RecyclerView.ViewHolder(binding.root){
    fun bind(

        orderModel: OrderModel,
        clickListener: (OrderModel?) -> Unit,
        position: Int
    ){


//        //if the orders date is same as above ones then we will hide the date display TV
//        if (datesList.get(position).equals(0)){
//            binding.dateTV.visibility = View.GONE
//        }
//        else{
//            binding.dateTV.visibility = View.VISIBLE
//            binding.dateTV.text = orderModel.date
//        }

        //in database the username has been saved under userId for admins ease of information
        binding.customerNameTV.text = orderModel.userName
        binding.orderIdTV.text = "OrderId:${orderModel.orderId}"
        binding.OrderValueTV.text = "OrderValue:${orderModel.orderValue}/-"
        binding.quantityTV.text = "Quantity:${orderModel.quantity}"
        binding.dateTV.text = "Date:${orderModel.date}"

        binding.cardview.setOnClickListener{
            clickListener(orderModel)
        }
    }
}

