package com.andrayudu.sureshdiaryfoods.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
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
import com.andrayudu.sureshdiaryfoods.utility.ProgressButton
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging

class CartActivity : AppCompatActivity() {

    private lateinit var binding:ActivityCartBinding
    private lateinit var cartViewModel: CartViewModel
    private lateinit var adapter:CartAdapter
    //this is the orderNow which has progressbar in it..
    lateinit var progressButton: ProgressButton

    private lateinit var actionBarBackButton: ImageView
    private lateinit var actionBarTextView: TextView

    //UI components
    private lateinit var limitTextView: TextView
    private lateinit var progressButtonTV:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_cart)
        val dao = FoodItemDatabase.getInstance(application).cartItemDao
        val repository = CartItemRepository(dao)
        val factory = CartViewModelFactory(repository)
        cartViewModel = ViewModelProvider(this,factory)[CartViewModel::class.java]
        binding.lifecycleOwner = this

        runtimeEnableAutoInit()

        actionBarBackButton = binding.actionbarCart.findViewById(R.id.actionbar_Back)
        actionBarTextView = binding.actionbarCart.findViewById(R.id.actionbar_Text)
        actionBarTextView.text = "Cart"
        progressButtonTV = binding.progressBtnOrderNow.findViewById(R.id.progressBtnText)
        progressButtonTV.text = "OrderNow"



        actionBarBackButton.setOnClickListener {
            onBackPressedDispatcher.addCallback(this,object :OnBackPressedCallback(true){
                override fun handleOnBackPressed() {

                    finish()
                }
            })
            onBackPressedDispatcher.onBackPressed()
        }



        limitTextView = binding.limitTV

        binding.progressBtnOrderNow.setOnClickListener {
            val btnName = "ORDERNOW"
            progressButton = ProgressButton(this,it,btnName)
            progressButton.buttonActivated()
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



        val observer  = Observer<List<CartItem>> {
            Log.i("TAG","cartItems ui is called")

            adapter.setList(it)
            cartViewModel.cartItemsCost()
            //this list will be uploaded to firebase soo we have to keep it up-to-date
            cartViewModel.cartItemsList.addAll(it)
            //if the cart has no items in it ,then we will close the activity
            if (it != null && it.size == 0) {
                finish()
            }
        }

        cartViewModel.getUserDetails().observe(this, Observer {
            //after getting userDetails only we will start calculation of total cost..
            //put a loading symbol ....
            //cartItems observe will start observing only after the userDetails is loaded because we need TransportRequired Atrribute
            cartViewModel.getCartItems().observe(this,observer)
            Log.i("TAG","the loading is done bigiluu")
            binding.idPBLoading.visibility = View.GONE
            binding.tDelivery.visibility  = View.VISIBLE
            binding.progressBtnOrderNow.visibility =View.VISIBLE
            limitTextView.append(it?.Limit.toString())
        })

        cartViewModel.getGrandTotal().observe(this, Observer {
            //the grand total is the final calculation that will be done
            //so it must definitely have an observer
            Log.i("TAG","grandtotal ui is called")
            updateUI(it)
        })

    }
    private fun updateUI(grandTotal:String?) {
        Log.i("TAG","update ui is called")
        binding.tTotal.text = getString(R.string.rupee_symbol) + " " + cartViewModel.getCartValue()
        binding.tGrandTotal.text = getString(R.string.rupee_symbol) + " " +grandTotal
        binding.tDelivery.text=getString(R.string.rupee_symbol) + " " + cartViewModel.getTransportValue()

    }

    private fun removeItem(cartItem: CartItem){
        //the received cartItem in parameters should be removed from the cartData
        cartViewModel.removeItem(cartItem)
    }


}