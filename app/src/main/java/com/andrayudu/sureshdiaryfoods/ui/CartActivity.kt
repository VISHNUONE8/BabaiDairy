package com.andrayudu.sureshdiaryfoods.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.FtsOptions.Order
import com.andrayudu.sureshdiaryfoods.CartViewModelFactory
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.adapters.CartAdapter
import com.andrayudu.sureshdiaryfoods.databinding.ActivityCartBinding
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.db.FoodItemDatabase
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.model.OrderModel
import com.andrayudu.sureshdiaryfoods.model.UserRegisterModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.*
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





    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_cart)
        val dao = FoodItemDatabase.getInstance(application).cartItemDao
        val repository = CartItemRepository(dao)
        val factory = CartViewModelFactory(repository)

        cartViewModel = ViewModelProvider(this,factory)[CartViewModel::class.java]

        binding.lifecycleOwner = this


          mAuth = Firebase.auth
        limitTextView = findViewById(R.id.limitTV)


        val userId = mAuth.currentUser?.uid
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

    private fun orderNowman() {
        TODO("Not yet implemented")
    }

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
        val user = mAuth.currentUser

        var max = Date().getTime().toInt()
        var orderId = max


        val current = LocalDateTime.now()
        val userId = user?.uid.toString()

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatted = current.format(formatter)
        val date = current.format(dateformatter)

        println("Current Date and Time is: $formatted")
       val ordersRef =  FirebaseDatabase.getInstance().getReference("CustomerOrders").child(userId)
       val ordersReference =  FirebaseDatabase.getInstance().getReference("Orders").child(userId)




        val order = OrderModel()
        order.userId = userId
        order.orderId = orderId.toString()
        order.quantity = "10"
        order.date = date
        order.orderValue = "200"
        order.cartItemList = cartItemsList

        ordersRef.child(order.orderId!!).setValue(order)
        for (cartItem in cartItemsList){
            //adding to the users orders list
           ordersReference.child(formatted.toString()).child(cartItem.Name).setValue(cartItem)
        }
        GlobalScope.launch {
            cartViewModel.repo.deleteAll()
        }


    }

    private fun listItemClicked(cartItem: CartItem){
        Toast.makeText(this,"Selected food is ${cartItem.Name}", Toast.LENGTH_SHORT).show()
    }
}