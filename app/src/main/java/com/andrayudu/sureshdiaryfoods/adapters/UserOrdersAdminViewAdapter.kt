package com.andrayudu.sureshdiaryfoods.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.layout.Layout
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.CustomerOrdersItemBinding
import com.andrayudu.sureshdiaryfoods.databinding.UserOrdersAdminViewItemBinding
import com.andrayudu.sureshdiaryfoods.model.OrderModel

class UserOrdersAdminViewAdapter(private val clickListener: (OrderModel?) -> Unit):RecyclerView.Adapter<UserOrdersHolder>() {

    //in this,the customers whose orders undelivered(pending) should only come
    private var NamesList = ArrayList<OrderModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserOrdersHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding: UserOrdersAdminViewItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.user_orders_admin_view_item,parent,false)
        return UserOrdersHolder(binding)
    }

    override fun getItemCount(): Int {
        return NamesList.size
    }

    override fun onBindViewHolder(holder: UserOrdersHolder, position: Int) {
        holder.bind(NamesList[position],clickListener)
    }

    fun setList(
        customerNamesList:ArrayList<OrderModel>
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
        clickListener: (OrderModel?) -> Unit
    ){

        //in database the username has been saved under userId for admins ease of information
        binding.dateTV.text = orderModel.userName

        binding.cardview.setOnClickListener{
            clickListener(orderModel)
        }
    }
}

