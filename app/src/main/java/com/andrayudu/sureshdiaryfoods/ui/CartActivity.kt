package com.andrayudu.sureshdiaryfoods.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrayudu.sureshdiaryfoods.Api
import com.andrayudu.sureshdiaryfoods.CartViewModelFactory
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.adapters.CartAdapter
import com.andrayudu.sureshdiaryfoods.databinding.ActivityCartBinding
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.db.FoodItemDatabase
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.model.OrderModel
import com.andrayudu.sureshdiaryfoods.model.UserRegisterModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.messaging.ktx.remoteMessage
import kotlinx.coroutines.*
import okhttp3.Callback
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class CartActivity : AppCompatActivity() {

    private lateinit var mAuth:FirebaseAuth
    private lateinit var limitTextView: TextView

    private lateinit var binding:ActivityCartBinding
    private lateinit var cartViewModel: CartViewModel
    private lateinit var adapter:CartAdapter

    private  var cartItemsList:ArrayList<CartItem> = ArrayList()

    var quantity = 0
    var name:String? = null
    var outstanding:String? = null
    var userId:String?=null






    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_cart)
        val dao = FoodItemDatabase.getInstance(application).cartItemDao
        val repository = CartItemRepository(dao)
        val factory = CartViewModelFactory(repository)


        runtimeEnableAutoInit()
        cartViewModel = ViewModelProvider(this,factory)[CartViewModel::class.java]

        binding.lifecycleOwner = this


          mAuth = Firebase.auth
        limitTextView = findViewById(R.id.limitTV)


         userId = mAuth.currentUser?.uid

        getUserName()
        val userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId.toString())

        binding.orderNowBtn.setOnClickListener {
            ordernow()
        }
        userReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    Log.i("snapshot","the snapshot exists bro")
                    val userRegisterModel = snapshot.getValue(UserRegisterModel::class.java)
                    limitTextView.append(userRegisterModel!!.Limit)
                }
            }


            override fun onCancelled(error: DatabaseError) {
            }

        })

        initRecyclerView()

    }

    fun runtimeEnableAutoInit() {
        // [START fcm_runtime_enable_auto_init]
        Firebase.messaging.isAutoInitEnabled = true
        // [END fcm_runtime_enable_auto_init]
    }

    private fun getUserName() {

        //for admin the order will be saved under the users name
        val userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)
        userReference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    name = snapshot.child("name").getValue<String>()
                    outstanding = snapshot.child("outstanding").getValue<String>()

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })    }


    private fun initRecyclerView(){
        binding.cartRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = CartAdapter({ selectedItem: CartItem?->listItemClicked(selectedItem!!)})
        binding.cartRecyclerView.adapter = adapter


        cartViewModel.cartItems.observe(this, Observer {
            Log.i("the cart list is :",""+it.toString())
            
                adapter.setList(it)
                cartViewModel.calculateGrandTotalCost()
                cartItemsList = it as ArrayList<CartItem>




//            cartItemsList = it as ArrayList<CartItem>

        })

        cartViewModel.grandTotal.observe(this, Observer {
            updateUI(it.toString())

        })


    }

    private fun updateUI(grandTotal: String) {
        binding.tTotal.text = getString(R.string.rupee_symbol) + " " + cartViewModel.getTotalCost()
        binding.tGrandTotal.text = getString(R.string.rupee_symbol) + " " + grandTotal
    }




    private fun ordernow() {

        val max = Date().getTime().toInt()
        val orderId = max


        val current = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatted = current.format(formatter)
        val date = current.format(dateformatter)

        println("Current Date and Time is: $formatted")


       val ordersReference =  FirebaseDatabase.getInstance().getReference("CustomerOrders").child(userId!!)
       val adminOrdersRef =  FirebaseDatabase.getInstance().getReference("Orders")






        val order = OrderModel()
        order.userId = userId
        order.orderId = orderId.toString()
        order.userName = name
        order.quantity =(cartItemsList.size).toString()
        order.date = date
        order.orderValue = cartViewModel.getTotalCost()
        order.cartItemList = cartItemsList
        if ((outstanding!!.toInt())>0){
            //order status -1 means the order is in waiting stage and will have to get acceptance from th admin ...
            order.orderStatus = "-1"
            val retrofit = Retrofit.Builder()
                .baseUrl("https://sureshdairyfoods-f8a5a.web.app/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(Api::class.java)
            val call:Call<ResponseBody> = api.sendNotification("Hii","Woohooo","anthera ayya aipoindiii")
            call.enqueue(object :retrofit2.Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {

                    try {
                        Toast.makeText(this@CartActivity,response.body().toString(),Toast.LENGTH_SHORT).show()
                    }
                    catch (e:IOException){
                        e.printStackTrace()
                    }

                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    Log.i("Sorry bro api call","failed")
                }
            })
        }
        else{
            order.orderStatus="0"
            //orderstatus 0 implies that the orderplaced succesfully

        }
        ordersReference.child(order.orderId!!).setValue(order)
        adminOrdersRef.child(order.orderId!!).setValue(order)
        //clearing the cart
        GlobalScope.launch {
            cartViewModel.repo.deleteAll()
        }


    }


    private fun listItemClicked(cartItem: CartItem){
        Toast.makeText(this,"Selected food is ${cartItem.Name}", Toast.LENGTH_SHORT).show()
    }
}