package com.andrayudu.sureshdiaryfoods.adapters


import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.FoodItemCardviewBinding
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.model.FoodItem
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import java.util.*

class FoodItemsRVAdapter(
    context: Context, private val cartRepo:CartItemRepository,
    private val clickListener: (FoodItem)->Unit, private val pencilListener: (FoodItem) -> Unit):RecyclerView.Adapter<MyViewHolder>() {

    private val foodItemsList = ArrayList<FoodItem>()
    private val mContext = context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        //this is where we will create listview
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding:FoodItemCardviewBinding = DataBindingUtil.inflate(layoutInflater,
            R.layout.food_item_cardview,parent,false)
        return MyViewHolder(binding,cartRepo)
    }

    override fun getItemCount(): Int {
        return foodItemsList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(mContext).load(foodItemsList[position].imageLink).into(holder.binding.iFoodImage)
        holder.bind(mContext,position,foodItemsList,foodItemsList[position],clickListener,pencilListener)
    }

    fun setList(foodItems:List<FoodItem>){
        foodItemsList.clear()
        foodItemsList.addAll(foodItems)
        notifyDataSetChanged()
    }
}


class MyViewHolder(val binding: FoodItemCardviewBinding, private val cartRepo: CartItemRepository):RecyclerView.ViewHolder(binding.root){

    private val tag = "MyViewHolder"
    fun bind(
         context: Context,position: Int,
        foodItemsList: ArrayList<FoodItem>,
        foodItem: FoodItem,
        clickListener: (FoodItem) -> Unit,
         pencilListener: (FoodItem) -> Unit
    ) {

        var Quantity = 0

        CoroutineScope(Dispatchers.IO).launch {
            val getCount = async(Dispatchers.IO) {
                //the above value is null if no items are addesd
                Quantity = cartRepo.getCartItemQuantity(foodItem.Name)
                //the quantity is null if the item doesnt exist in the cart coalesce or ifnull functions can be used

            }
            getCount.await()
            withContext(Dispatchers.Main) {

                binding.tCount.setText(Quantity.toString())
                foodItemsList[position].Quantity = Quantity

            }

        }


        binding.tFoodName.text = foodItem.Name
        binding.tPrice.text = (" ₹ ${foodItem.Price}")


        binding.iPlus.setOnClickListener{

            foodItemsList.get(bindingAdapterPosition).Quantity= ((foodItemsList.get(bindingAdapterPosition).Quantity) + 1)
            binding.tCount.setText(foodItemsList.get(bindingAdapterPosition).Quantity.toString())
            //now we should update this into cart database on demand
            clickListener(foodItem)


        }
        binding.iMinus.setOnClickListener{

            if (foodItemsList[bindingAdapterPosition].Quantity!=0) {


                foodItemsList[bindingAdapterPosition].Quantity =
                    ((foodItemsList[bindingAdapterPosition].Quantity) - 1)

                binding.tCount.setText(foodItemsList.get(bindingAdapterPosition).Quantity.toString())
                //now we should update this into cart database on demand


                clickListener(foodItem)
            }

        }

        binding.pencilIV.setOnClickListener {
            pencilListener(foodItem)
        }
    }



}