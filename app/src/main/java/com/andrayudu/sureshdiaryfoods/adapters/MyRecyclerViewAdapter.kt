package com.andrayudu.sureshdiaryfoods


import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.compose.ui.text.input.ImeOptions
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andrayudu.sureshdiaryfoods.databinding.FoodItemCardviewBinding
import com.andrayudu.sureshdiaryfoods.db.FoodItemDatabase
import com.andrayudu.sureshdiaryfoods.model.FoodItem
import kotlinx.coroutines.*
import java.util.*

class MyRecyclerViewAdapter(private val context: Context,private val clickListener: (FoodItem)->Unit,private val pencilListener: (FoodItem) -> Unit):RecyclerView.Adapter<MyViewHolder>() {

    
    private val foodItemsList = ArrayList<FoodItem>()
    private val mContext = context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        //this is where we will create listview
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding:FoodItemCardviewBinding = DataBindingUtil.inflate(layoutInflater,R.layout.food_item_cardview,parent,false)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return foodItemsList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(mContext,position,foodItemsList,foodItemsList[position],clickListener,pencilListener)
    }

    fun setList(foodItems:List<FoodItem>){
        foodItemsList.addAll(foodItems)
        notifyDataSetChanged()
    }




}


class MyViewHolder(val binding:FoodItemCardviewBinding):RecyclerView.ViewHolder(binding.root){

    private val tag = "MyViewHolder"
    fun bind(
         context: Context,position: Int,
        foodItemsList: ArrayList<FoodItem>,
        foodItem: FoodItem,
        clickListener: (FoodItem) -> Unit,
         pencilListener: (FoodItem) -> Unit
    ){

        var Quantity = "0"

        GlobalScope.launch {
            val getCount = async(Dispatchers.IO){
                Quantity = FoodItemDatabase.getInstance(context).cartItemDao.getCartCount(foodItem.Name)
                //the quantity is null if the item doesnt exist in the cart coalesce or ifnull functions can be used

            }
            getCount.await()
            withContext(Dispatchers.Main){
                binding.tCount.setText(Quantity)
                if (Quantity!=null){
                    foodItemsList.get(position).Quantity = Quantity
                }
            }
        }


        binding.tFoodName.text = foodItem.Name
        binding.tPrice.text = (" ₹ ${foodItem.Price}")

//        binding.tCount.setOnEditorActionListener(object :OnEditorActionListener{
//            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
//                if (actionId == EditorInfo.IME_ACTION_DONE)
//                {
//                    foodItemsList.get(position).Quantity= v?.text.toString()
//
//                    binding.tCount.setText(foodItemsList.get(position).Quantity.toString())
//                    //now we should update this into cart database on demand
//                    clickListener(foodItem)
//
//                    return true
//                }
//                return false
//            }
//        })

        binding.iPlus.setOnClickListener{

            foodItemsList.get(bindingAdapterPosition).Quantity= ((foodItemsList.get(bindingAdapterPosition).Quantity!!).toInt() + 1).toString()
            binding.tCount.setText(foodItemsList.get(bindingAdapterPosition).Quantity.toString())
            //now we should update this into cart database on demand
            clickListener(foodItem)


        }
        binding.iMinus.setOnClickListener{

            if (foodItemsList.get(bindingAdapterPosition).Quantity!!.toInt() !=0) {


                foodItemsList.get(bindingAdapterPosition).Quantity =
                    ((foodItemsList.get(bindingAdapterPosition).Quantity)!!.toInt() - 1).toString()

                binding.tCount.setText(foodItemsList.get(bindingAdapterPosition).Quantity.toString())
                //now we should update this into cart database on demand

                Log.i("the list you want is:", "" + foodItemsList.toString())
                clickListener(foodItem)
            }

        }

        binding.pencilIV.setOnClickListener {
            pencilListener(foodItem)
        }
    }



}