package com.andrayudu.sureshdiaryfoods.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrayudu.sureshdiaryfoods.CartViewModelFactory
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.adapters.CartAdapter
import com.andrayudu.sureshdiaryfoods.databinding.ActivityCartBinding
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.db.FoodItemDatabase
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

class CartActivity : AppCompatActivity() {

    private lateinit var binding:ActivityCartBinding
    private lateinit var cartViewModel: CartViewModel
    private lateinit var adapter:CartAdapter

    //UI components
    private lateinit var limitTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_cart)
        val dao = FoodItemDatabase.getInstance(application).cartItemDao
        val repository = CartItemRepository(dao)
        val factory = CartViewModelFactory(repository)
        cartViewModel = ViewModelProvider(this,factory)[CartViewModel::class.java]
        binding.lifecycleOwner = this

        runtimeEnableAutoInit()

        cartViewModel.loadUserDetails()

        limitTextView = findViewById(R.id.limitTV)

        binding.orderNowBtn.setOnClickListener {
            cartViewModel.ordernow()
        }


        initRecyclerView()
    }

    //enables firebasecloudmessaging by default it is enabled only , but we are making sure once again as
    //we need to send notification to admin incase of outstanding amount value exists...
    fun runtimeEnableAutoInit() {
        // [START fcm_runtime_enable_auto_init]
        Firebase.messaging.isAutoInitEnabled = true
        // [END fcm_runtime_enable_auto_init]
    }

    private fun initRecyclerView(){
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CartAdapter({ selectedItem: CartItem?->removeItem(selectedItem!!)})
        binding.cartRecyclerView.adapter = adapter



        val observer  = Observer<List<CartItem>> {  }

        cartViewModel.getUserDetails().observe(this, Observer {
            //after change in the userDetails only the calculation of totalCost begins else just blank
            //put a loading symbol ....
            Log.i("TAG","the loading is done bigiluu")
            cartViewModel.getCartItems().observe(this,observer)
            binding.idPBLoading.visibility = View.GONE
            binding.tDelivery.visibility  = View.VISIBLE
            binding.orderNowBtn.visibility =View.VISIBLE
            limitTextView.append(it?.Limit)
            cartViewModel.cartItemsCost()
            updateUI()
        })

        cartViewModel.getCartItems().observe(this, Observer {
            Log.i("TAG","cartItems ui is called")

            adapter.setList(it)
            cartViewModel.cartItemsCost()
            //this list will be uploaded to firebase soo we have to keep it up-to-date
            cartViewModel.cartItemsList.addAll(it)
            updateUI()
            //if the cart has no items in it ,then we will close the activity
            if (it != null && it.size == 0) {
                finish()
            }
        })
        cartViewModel.getGrandTotal().observe(this, Observer {
            Log.i("TAG","grandtotal ui is called")
            binding.tGrandTotal.text = getString(R.string.rupee_symbol) + " " +it
        })
        cartViewModel.getTransportCharges().observe(this, Observer {
            Log.i("TAG","transport ui is called")
            binding.tDelivery.text=getString(R.string.rupee_symbol) + " " + it
        })
    }
    private fun updateUI() {
        Log.i("TAG","update ui is called")
        binding.tTotal.text = getString(R.string.rupee_symbol) + " " + cartViewModel.getCartValue()
        binding.tGrandTotal.text = getString(R.string.rupee_symbol) + " " + cartViewModel.getGrandTotal().value
        binding.tDelivery.text=getString(R.string.rupee_symbol) + " " + cartViewModel.getTransportValue()


    }

    private fun removeItem(cartItem: CartItem){
        //the received cartItem in parameters should be removed from the cartData
        cartViewModel.removeItem(cartItem)
    }
}