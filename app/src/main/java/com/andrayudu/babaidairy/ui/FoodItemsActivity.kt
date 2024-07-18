package com.andrayudu.babaidairy.ui

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrayudu.babaidairy.model.CartItem
import com.andrayudu.babaidairy.adapters.FoodItemsRVAdapter
import com.andrayudu.babaidairy.R
import com.andrayudu.babaidairy.databinding.ActivityFoodItemsBinding
import com.andrayudu.babaidairy.db.CartItemRepository
import com.andrayudu.babaidairy.db.FoodItemDatabase
import com.andrayudu.babaidairy.model.FoodItem
import com.andrayudu.babaidairy.model.ItemsCatalogueModel

class FoodItemsActivity : AppCompatActivity() {

    private val TAG = "FoodItemsActivity"

    private lateinit var foodItemsViewModel: FoodItemsViewModel
    private lateinit var itemCategoryFromIntent: String
    private lateinit var itemsCatalogue:ItemsCatalogueModel

    //UI components
    private lateinit var actionBarBackButton: ImageView
    private lateinit var actionBarTextView: TextView
    private lateinit var tTotalCost:TextView
    private lateinit var tCartQuantity:TextView
    private lateinit var binding:ActivityFoodItemsBinding
    private lateinit var adapter: FoodItemsRVAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_food_items)
        val dao = FoodItemDatabase.getInstance(application).cartItemDao
        val repository = CartItemRepository(dao)
        val factory = FoodItemsViewModelFactory(repository)

        foodItemsViewModel = ViewModelProvider(this,factory)[FoodItemsViewModel::class.java]
        binding.myViewModel = foodItemsViewModel
        binding.lifecycleOwner = this


         itemCategoryFromIntent = intent.getStringExtra("itemName").toString()
         itemsCatalogue = intent.getParcelableExtra<ItemsCatalogueModel>("itemsCatalogue")!!


        initViews(itemCategoryFromIntent)
        initObservers()
        initClickListeners()
        initRecyclerView()

        //we are passing the itemsCatalogue,itemCategoryFromIntent which are received from Intent..
        foodItemsViewModel.getSpecialPricesList(itemsCatalogue,itemCategoryFromIntent)

    }

    override fun onResume() {
        super.onResume()
        foodItemsViewModel.firebaseFoodItems
    }

    private fun initObservers() {

        foodItemsViewModel.cartItems.observe(this) {

            updateCartUI(it)

        }
        foodItemsViewModel.firebaseFoodItems.observe(this) {
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

    private fun initViews(itemCategoryFromIntent: String?) {
        actionBarBackButton = binding.actionBarFoodItems.findViewById(R.id.actionbar_Back)
        actionBarTextView = binding.actionBarFoodItems.findViewById(R.id.actionbar_Text)
        tTotalCost = binding.tTotalPrice
        tCartQuantity = binding.tCartCount
        actionBarTextView.text = itemCategoryFromIntent

    }

    private fun initRecyclerView(){
        val dao = FoodItemDatabase.getInstance(this.application).cartItemDao
        val cartRepo = CartItemRepository(dao)

        binding.foodItemsRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = FoodItemsRVAdapter(this,cartRepo,{ selectedItem:FoodItem->listItemClicked(selectedItem)},
            {selectedItem:FoodItem->pencilClicked(selectedItem)})
        binding.foodItemsRecyclerView.adapter = adapter
    }

    private fun updateCartUI(cartItems: List<CartItem>?) {
        if(cartItems!=null && cartItems.isNotEmpty()){
            binding.cartView.visibility = View.VISIBLE
            var price =0

            for (cartItem in cartItems) {

                    price += cartItem.ItemTotalPrice!!.toInt()

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

    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }



    //invokes on clicking the recyclerview items
    private fun listItemClicked(foodItem: FoodItem){
        foodItemsViewModel.insert(foodItem)
        hideKeyboard(this)
    }

    //invokes on clicking the recyclerview items
    private fun pencilClicked(foodItem: FoodItem){
      showAlertDialogButtonClicked(foodItem)
    }

    //this dialog displays a editText for adding items by entering number...
    fun showAlertDialogButtonClicked(foodItem: FoodItem) {
        // Create an alert builder
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Enter Quantity")

        // set the custom layout
        val customLayout: View = layoutInflater.inflate(R.layout.quantity_dialog, null)
        builder.setView(customLayout)
        customLayout.findViewById<EditText>(R.id.quantityEt).requestFocus()

        // add a button
        builder.setPositiveButton("OK") { dialog: DialogInterface?, which: Int ->
            // send data from the AlertDialog to the Activity
            val editText = customLayout.findViewById<EditText>(R.id.quantityEt)
            foodItem.Quantity = editText.text.toString().toInt()
            foodItemsViewModel.insert(foodItem)
            adapter.notifyDataSetChanged()
        }
        builder.setNegativeButton("Cancel",(DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        }))
        // create and show the alert dialog
        val dialog = builder.create()
        dialog.show()
    }



}