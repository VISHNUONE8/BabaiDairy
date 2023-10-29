package com.andrayudu.sureshdiaryfoods.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.adapters.CartAdapter
import com.andrayudu.sureshdiaryfoods.databinding.ActivityCartBinding
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.db.FoodItemDatabase
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.utility.ProgressButton

class CartActivity : AppCompatActivity() {

    private val tag= "CartActivity"

    private lateinit var binding:ActivityCartBinding
    private lateinit var cartViewModel: CartViewModel
    private lateinit var adapter:CartAdapter

    //UI components
    private lateinit var progressButtonTV:TextView
    private lateinit var progressButton: ProgressButton
    private lateinit var actionBarBackButton: ImageView
    private lateinit var actionBarTextView: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_cart)

        val dao = FoodItemDatabase.getInstance(application).cartItemDao
        val repository = CartItemRepository(dao)
        val factory = CartViewModelFactory(repository)
        cartViewModel = ViewModelProvider(this,factory)[CartViewModel::class.java]
        binding.lifecycleOwner = this


        initViews()
        initClickListeners()
        initRecyclerView()
        //the main work of this activity starts from here
        initObservers()

        cartViewModel.runtimeEnableAutoInit()

    }




    private fun initClickListeners() {


        actionBarBackButton.setOnClickListener {
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    finish()
                }
            })
            onBackPressedDispatcher.onBackPressed()
        }




        binding.progressBtnOrderNow.setOnClickListener {
            val btnName = "ORDERNOW"
            progressButton = ProgressButton(this, it, btnName)
            progressButton.buttonActivated()
            cartViewModel.placeOrder()
        }
    }

    private fun initViews() {
        actionBarBackButton = binding.actionbarCart.findViewById(R.id.actionbar_Back)
        actionBarTextView = binding.actionbarCart.findViewById(R.id.actionbar_Text)
        actionBarTextView.text = "Cart"
        progressButtonTV = binding.progressBtnOrderNow.findViewById(R.id.progressBtnText)
        progressButtonTV.text = "OrderNow"

        binding.clearCartTV.setOnClickListener{
            cartViewModel.clearCart()
            Toast.makeText(this,"Cart has been Cleared",Toast.LENGTH_SHORT).show()
        }
    }


    private fun initRecyclerView(){
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CartAdapter({ selectedItem: CartItem?->removeItem(selectedItem!!)})
        binding.cartRecyclerView.adapter = adapter

    }

    private fun initObservers() {
        //cartItems observer..
        val observer = Observer<List<CartItem>> {
            adapter.setList(it)
            cartViewModel.cartItemsCost()
            //if the cart has no items in it ,then we will close the activity
            if (it != null && it.isEmpty()) {
                finish()
            }
        }

        //after getting userDetails only we will start calculation of total cost..
        //put a loading symbol ....
        //cartItems observe will start observing only after the userDetails is loaded because we need TransportRequired Atrribute
        cartViewModel.getUserDetails().observe(this, Observer {

            cartViewModel.getCartItems().observe(this, observer)
            Log.i(tag, "User details have been loaded successfully")

        })

        cartViewModel.getGrandTotal().observe(this, Observer {
            //the grand total is the final calculation that will be done
            //so it must definitely have an observer
            Log.i(tag, "grandtotal is updated...")
            updateUI(it)
            binding.idPBLoading.visibility = View.GONE
            binding.tDelivery.visibility = View.VISIBLE
            binding.progressBtnOrderNow.visibility = View.VISIBLE
        })

        cartViewModel.getStatusLive().observe(this, Observer {
            if (it!=null){
                if ( it.equals("Limit")) {
                    progressButton.buttonFinished()
                    Toast.makeText(
                        this,
                        "Limit Exceeded:\nplease contact the Admin..",
                        Toast.LENGTH_LONG
                    ).show()
                }
                else if (it.equals("Success")){
                    Toast.makeText(this,"Order Placed Successfully",Toast.LENGTH_LONG).show()
                    val intent = Intent(this,HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
            }

        })
    }

    private fun updateUI(grandTotal:String?) {
        Log.i(tag,"updateUI function is called")
        binding.tTotal.text = getString(R.string.rupee_symbol) + " " + cartViewModel.getCartValue()
        binding.tGrandTotal.text = getString(R.string.rupee_symbol) + " " +grandTotal
        binding.tDelivery.text=getString(R.string.rupee_symbol) + " " + cartViewModel.getTransportValue()

    }

    //removes the received cartItem in parameters  from the cartData
    private fun removeItem(cartItem: CartItem){
        cartViewModel.removeItem(cartItem)
    }





}