package com.andrayudu.sureshdiaryfoods.ui

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.adapters.CartRVAdapter
import com.andrayudu.sureshdiaryfoods.databinding.ActivityCartBinding
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.db.FoodItemDatabase
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.utility.ProgressButton
import com.razorpay.Checkout
import com.razorpay.ExternalWalletListener
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import org.json.JSONObject
import org.w3c.dom.Text

class CartActivity : AppCompatActivity(),PaymentResultWithDataListener,ExternalWalletListener,DialogInterface.OnClickListener {

    private val tag= "CartActivity"

    private lateinit var cartViewModel: CartViewModel

    private lateinit var alertDialogBuilder: AlertDialog.Builder

    //UI components
    private lateinit var progressButton: ProgressButton
    private lateinit var progressBtnOrderNow:View
    private lateinit var actionBarBackButton: ImageView
    private lateinit var actionBarTextView: TextView
    private lateinit var adapter:CartRVAdapter
    private lateinit var binding:ActivityCartBinding




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_cart)

        val dao = FoodItemDatabase.getInstance(application).cartItemDao
        val repository = CartItemRepository(dao)
        val factory = CartViewModelFactory(repository)
        cartViewModel = ViewModelProvider(this,factory)[CartViewModel::class.java]
        binding.lifecycleOwner = this


        initViews()
        initProgressBtn()
        initClickListeners()
        initRecyclerView()
        //the main work of this activity starts from here
        initObservers()

        Checkout.preload(applicationContext)
        alertDialogBuilder = AlertDialog.Builder(this@CartActivity)
        alertDialogBuilder.setTitle("PaymentResult")
        alertDialogBuilder.setCancelable(true)
        alertDialogBuilder.setPositiveButton("Ok",this)
        val button:View = findViewById(R.id.progressBtnPayNow)
        val buttonText: TextView = button.findViewById(R.id.progressBtnText)
        buttonText.text = "PayNow"
        button.setOnClickListener {
            startPayment()
        }


        cartViewModel.runtimeEnableAutoInit()

    }

    private fun startPayment() {


        /*
        *  You need to pass current activity in order to let Razorpay create CheckoutActivity
        * */
        val activity: Activity = this
        val checkout = Checkout()
//        val etApiKey = findViewById<EditText>(R.id.et_api_key)
//        val etCustomOptions = findViewById<EditText>(R.id.et_custom_options)
        if (!TextUtils.isEmpty(binding.tGrandTotal.text.toString())){
            checkout.setKeyID("rzp_test_K6KTKSGEynrk1s")
        }
        try {
            var options = JSONObject()
            if (TextUtils.isEmpty(binding.tGrandTotal.text.toString())){
//                options = JSONObject(etCustomOptions.text.toString())
            }else{
                options.put("name","Razorpay Corp")
                options.put("description","Demoing Charges")
                //You can omit the image option to fetch the image from dashboard
                options.put("image","https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
                options.put("currency","INR")
                options.put("amount","100")
                options.put("send_sms_hash",true);

                val prefill = JSONObject()
                prefill.put("email","test@razorpay.com")
                prefill.put("contact","9021066696")

                options.put("prefill",prefill)
            }

            checkout.open(activity,options)
        }catch (e: Exception){
            Toast.makeText(activity,"Error in payment: "+ e.message,Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }


    private fun initProgressBtn() {
        val btnName = "ORDER NOW"
        progressBtnOrderNow = binding.progressBtnOrderNow
        progressButton = ProgressButton(this, progressBtnOrderNow, btnName)
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
            progressButton.buttonActivated()
            cartViewModel.placeOrder()
        }
        binding.clearCartTV.setOnClickListener{
            cartViewModel.clearCart()
        }


    }

    private fun initViews() {
        actionBarBackButton = binding.actionbarCart.findViewById(R.id.actionbar_Back)
        actionBarTextView = binding.actionbarCart.findViewById(R.id.actionbar_Text)
        actionBarTextView.text = "Cart"


    }


    private fun initRecyclerView(){
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CartRVAdapter { selectedItem: CartItem? -> removeItem(selectedItem) }
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
            if (it!=null) {
                if (it.equals("Limit")) {
                    progressButton.buttonFinished()
                    Toast.makeText(
                        this,
                        "Limit Exceeded:\nplease contact the Admin..",
                        Toast.LENGTH_LONG
                    ).show()
                } else if (it.equals("Success")) {
                    progressButton.buttonFinished()
                    Toast.makeText(this, "Order Placed Successfully", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                } else if (it.equals("Hold")) {
                    progressButton.buttonFinished()
                    Toast.makeText(this, "Application is under Maintenance ,\nPlease Contact Admin...", Toast.LENGTH_LONG).show()
                }
                else if (it.equals("Deleted")){
                    Toast.makeText(this,"Cart has been Cleared",Toast.LENGTH_SHORT).show()
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

    //removes the cartItem on clicking cross in cartRV
    private fun removeItem(cartItem: CartItem?){
        if (cartItem != null) {
            cartViewModel.removeItem(cartItem)
        }
    }

    override fun onPaymentSuccess(p0: String?, p1: PaymentData?) {
        try{
            alertDialogBuilder.setMessage("Payment Successful : Payment ID: $p0\nPayment Data: ${p1?.data}")
            alertDialogBuilder.show()
        }catch (e: Exception){
            e.printStackTrace()
        }
    }

    override fun onPaymentError(p0: Int, p1: String?, p2: PaymentData?) {
        try {
            alertDialogBuilder.setMessage("Payment Failed : Payment Data: ${p2?.data}")
            alertDialogBuilder.show()
        }catch (e: Exception){
            e.printStackTrace()
        }    }

    override fun onExternalWalletSelected(p0: String?, p1: PaymentData?) {
        try{
            alertDialogBuilder.setMessage("External wallet was selected : Payment Data: ${p1?.data}")
            alertDialogBuilder.show()
        }catch (e: Exception){
            e.printStackTrace()
        }    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
    }


}