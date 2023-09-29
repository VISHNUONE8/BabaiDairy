package com.andrayudu.sureshdiaryfoods.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrayudu.sureshdiaryfoods.MyRecyclerViewAdapter
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.ActivityFoodItemsBinding
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.db.FoodItemDatabase
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.model.FoodItem

class FoodItemsActivity : AppCompatActivity() {



    private val tag = "FoodItemsActivity"

    private lateinit var binding:ActivityFoodItemsBinding
    private lateinit var foodItemsViewModel: FoodItemsViewModel
    private lateinit var adapter: MyRecyclerViewAdapter
    private var itemName:String? = null

    //UI components
    private lateinit var actionBarBackButton: ImageView
    private lateinit var actionBarTextView: TextView
    private lateinit var tTotalCost:TextView
    private lateinit var tCartQuantity:TextView




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_food_items)
        val dao = FoodItemDatabase.getInstance(application).cartItemDao
        val repository = CartItemRepository(dao)
        val factory = FoodItemsViewModelFactory(repository)
        foodItemsViewModel = ViewModelProvider(this,factory)[FoodItemsViewModel::class.java]
        binding.myViewModel = foodItemsViewModel
        binding.lifecycleOwner = this


        itemName = intent.getStringExtra("itemName")

        initViews()
        initObservers()
        initClickListeners()
        initRecyclerView()


        foodItemsViewModel.getSpecialPricesSnapshot()



    }

    private fun initObservers() {

        foodItemsViewModel.cartItems.observe(this) {

            updateCartUI(it)

        }

        foodItemsViewModel.getStatus().observe(this) {
            if (it != null) {
                //indicates the special prices snapshot is loaded...
                if (it.equals("loaded")) {
                    Log.i(tag, "calling LoadItems now...")
                    foodItemsViewModel.loadItems(itemName)
                }
            }
        }

        foodItemsViewModel.getFirebaseFoodItems().observe(this) {
            Log.i("MY TAG", it.toString())
            //as soon as the items load we will hide the progress bar
            binding.idPBLoading.visibility = View.INVISIBLE
            adapter.setList(it)
        }
    }

    private fun initClickListeners() {


        binding.bCart.setOnClickListener {
            startActivity(Intent(this, CartActivity::class.java))
        }

        actionBarBackButton.setOnClickListener {
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    finish()
                }
            })
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun initViews() {
        actionBarBackButton = binding.actionBarFoodItems.findViewById(R.id.actionbar_Back)
        actionBarTextView = binding.actionBarFoodItems.findViewById(R.id.actionbar_Text)
        tTotalCost = binding.tTotalPrice
        tCartQuantity = binding.tCartCount
        actionBarTextView.text = itemName

    }

    private fun initRecyclerView(){
        binding.foodItemsRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = MyRecyclerViewAdapter(this){selectedItem:FoodItem->listItemClicked(selectedItem)}
        binding.foodItemsRecyclerView.adapter = adapter
    }

    private fun updateCartUI(cartItems: List<CartItem>?) {
        if(cartItems!=null && cartItems.isNotEmpty()){
            binding.cartView.visibility = View.VISIBLE
            var price =0

            for (cartItem in cartItems) {
                price += (cartItem.Price!!.toInt() * cartItem.Quantity!!.toInt())
//                quantity = quantity + cartItem.Quantity.toInt()
            }
            tCartQuantity.text = (cartItems.size.toString())
            tTotalCost.text = (getString(R.string.rupee_symbol_new,price.toString()))

        }
        else
        {
            binding.cartView.visibility = (View.GONE)
            tCartQuantity.text = "0"
            tTotalCost.text = getString(R.string.rupee_symbol_new,"0")
        }

    }


    //invokes on clicking the recyclerview items
    private fun listItemClicked(foodItem: FoodItem){
            foodItemsViewModel.insert(foodItem)
    }


}