package com.andrayudu.sureshdiaryfoods.adapters

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.LayoutCartItemBinding
import com.andrayudu.sureshdiaryfoods.databinding.SubitemCardviewBinding
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.model.FoodItem

class CartAdapter( private val clickListener: (CartItem?)->Unit) : RecyclerView.Adapter<CartItemsViewHolder>(){

    private val foodItemsList = ArrayList<CartItem>()
    private val tag = "CartAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartItemsViewHolder {
        val layoutInflater
                = LayoutInflater.from(parent.context)
        val binding : LayoutCartItemBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.layout_cart_item,parent,false)
        return CartItemsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return foodItemsList.size

    }

    override fun onBindViewHolder(holder: CartItemsViewHolder, position: Int) {
        holder.bind(foodItemsList[position],clickListener)
    }

    fun setList(cartItems:List<CartItem>){
        foodItemsList.clear()
        foodItemsList.addAll(cartItems)
        notifyDataSetChanged()

    }
}

class CartItemsViewHolder(val binding: LayoutCartItemBinding):RecyclerView.ViewHolder(binding.root){
    private val tag = "CartItemsViewHolder"

    fun bind(
        cartItem: CartItem?,
        clickListener: (CartItem?) -> Unit
    ){

        if (cartItem!=null){

            binding.tName.text = cartItem.Name
            binding.tPrice.text = "₹ "+cartItem.Price
            Log.i(tag,"the category is :"+cartItem.Category)
            if(cartItem.Category.equals("Kova") || cartItem.Category.equals("KovaSpl")){
                val kovaInKgs = cartItem.Quantity!!.toInt() * 3
                binding.tQuantity.text = "${kovaInKgs} kgs    (${cartItem.Quantity} boxes) "
                binding.tTotalPrice.text = "₹ "+(cartItem.Quantity!!.toInt() * cartItem.Price!!.toInt() * 3)
            }
            else{
                binding.tQuantity.text = cartItem.Quantity
                binding.tTotalPrice.text = "₹ "+(cartItem.Quantity!!.toInt() * cartItem.Price!!.toInt())

            }

        }

        binding.iDelete.setOnClickListener{
            //onClicking the delete icon , the cartItem willbe passed into the function which will remove it from cartlist table
            clickListener(cartItem)
        }

//        binding.itemHolder.setOnClickListener{
//            clickListener(specializationType)
//        }




    }
}