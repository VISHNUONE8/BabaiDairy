package com.andrayudu.sureshdiaryfoods.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrayudu.sureshdiaryfoods.FoodItemsViewModelFactory
import com.andrayudu.sureshdiaryfoods.MyRecyclerViewAdapter
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.ActivityFoodItemsBinding
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.db.FoodItemDatabase
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.model.FoodItem
import kotlinx.coroutines.*

class FoodItemsActivity : AppCompatActivity() {


    private lateinit var binding:ActivityFoodItemsBinding
    private lateinit var foodItemsViewModel: FoodItemsViewModel
    private lateinit var adapter: MyRecyclerViewAdapter

    private lateinit var tTotalCost:TextView
    private lateinit var tCartQuantity:TextView



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_food_items)
        val dao = FoodItemDatabase.getInstance(application).cartItemDao
        val repository = CartItemRepository(dao)
        val factory = FoodItemsViewModelFactory(repository)
        foodItemsViewModel = ViewModelProvider(this,factory)[FoodItemsViewModel::class.java]





        tTotalCost = binding.cartView.findViewById(R.id.t_total_price)
        tCartQuantity = binding.cartView.findViewById(R.id.t_cart_count)
        binding.myViewModel = foodItemsViewModel
        binding.lifecycleOwner = this


        binding.bCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        foodItemsViewModel.message.observe(this, Observer{
            it.getContentIfNotHandled()?.let {
                Toast.makeText(this,it, Toast.LENGTH_SHORT).show()
            }
        })



        val itemName = intent.getStringExtra("itemName")
        println(itemName+"is the item name")

        val relLayout = binding.actionBarFoodItems

        val headText:TextView = relLayout.findViewById(R.id.actionbar_Home_Text)
        headText.text = itemName

        initRecyclerView()

        // until the firebase values load we will be displaying a loading symbol

        CoroutineScope(Dispatchers.IO).launch {
            val special= async {
                foodItemsViewModel.getSpecialPricesSnapshot()
            }
            special.await()
            foodItemsViewModel.getFirebaseData(itemName)


        }



    }

    private fun initRecyclerView(){
        binding.foodItemsRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MyRecyclerViewAdapter(this,{selectedItem:FoodItem->listItemClicked(selectedItem)})
        binding.foodItemsRecyclerView.adapter = adapter



        displayFoodItemsList()


        foodItemsViewModel.cartItems.observe(this, Observer {

            updateCartUI(it)

        })


    }

    private fun updateCartUI(cartItems: List<CartItem>?) {
        if(cartItems!=null && cartItems.size > 0){
            binding.cartView.visibility = View.VISIBLE
            var price =0
            var quantity = 0

            for (cartItem in cartItems) {
                price = price +( cartItem.Price.toInt() * cartItem.Quantity.toInt())
//                quantity = quantity + cartItem.Quantity.toInt()
            }
            tCartQuantity.setText(cartItems.size.toString())
            tTotalCost.setText(getString(R.string.rupee_symbol) + price.toString())

        }
        else
        {
            binding.cartView.setVisibility(View.GONE)
            tCartQuantity.text = "0"
            tTotalCost.text = getString(R.string.rupee_symbol) + "0"
        }

    }

    private fun displayFoodItemsList(){
        foodItemsViewModel.firebaseFoodItems.observe(this, Observer {
            Log.i("MY TAG",it.toString())
            //as soon as the items load we will hide the progress bar
            binding.idPBLoading.visibility = View.INVISIBLE
            adapter.setList(it)
            adapter.notifyDataSetChanged()
        })
    }


    private fun listItemClicked(foodItem: FoodItem){
        Toast.makeText(this,"Selected food is ${foodItem.Name}",Toast.LENGTH_SHORT).show()
            foodItemsViewModel.insert(foodItem)

    }

}