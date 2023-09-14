package com.andrayudu.sureshdiaryfoods.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrayudu.sureshdiaryfoods.Api
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.model.OrderModel
import com.andrayudu.sureshdiaryfoods.model.UserRegisterModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class CartViewModel(private val repository: CartItemRepository):ViewModel() {

    var repo = repository
    private val grandTotal = MutableLiveData<String?>()
    private val cartItems = repository.cartItems
    private val transportCharges = MutableLiveData<String?>()
    private val userDetails = MutableLiveData<UserRegisterModel?>()
    //this list will be uploaded to firebase
    var cartItemsList: ArrayList<CartItem> = ArrayList()

    var userLimit:String? = null
    private var cartValue: Int? = 0
    private var transportValue: Int? = 0

    var name:String? = null
    var outstanding:String? = null
    var transportRequired:String? = null
    val mAuth = FirebaseAuth.getInstance()
    var userId = mAuth.currentUser?.uid



    fun getCartValue(): String {
        return cartValue.toString()
    }
    fun getTransportValue(): String {
        return transportValue.toString()
    }

    //this function calculates the total cost of items in the cart(without transport)
    fun cartItemsCost() {

        val cartList = cartItems.value
        if(cartList!=null) {
            cartValue = 0
            for (cartItem in cartList) {
                cartValue =
                    (cartValue!!.toInt() + (cartItem.Price.toInt() * cartItem.Quantity.toInt()))
            }
        }
        //for calculating the transportCost or anything related to it,we should make sure that userDetails are available with us soo...
            viewModelScope.launch(Dispatchers.IO) {
                val one = async { calculateTransportCharges() }
                one.await()
                withContext(Dispatchers.Main) {
                    //after getting the customer details if he doesnt use our transport then we will put 0 in transport charges...
                    grandTotal.setValue((cartValue!! + (transportCharges.value)!!.toInt()).toString())
                }
            }
    }
    private suspend fun calculateTransportCharges() {
        //first we will get Kova Quantity count and then we will calculate transport charges
        Log.i("TAG","transport required is"+transportRequired)
        //if the user requires transport then only we will call database call else we will put transport charges 0
        if (transportRequired == "yes"){
            transportValue =repository.getKovaCount("Kova")
            transportCharges.postValue(transportValue.toString())
            }
        else{
            //already transportVal is 0 when declared
            transportCharges.postValue(transportValue.toString())
        }

    }


    fun getCartItems():LiveData<List<CartItem>>{
        return cartItems
    }
    fun getGrandTotal():LiveData<String?>{
        return grandTotal
    }
    fun getTransportCharges():LiveData<String?>{
        return transportCharges
    }
    fun getUserDetails():LiveData<UserRegisterModel?>{
        return userDetails
    }

    // this fun is used to get the customers details like name,outstanding balance
      fun loadUserDetails() {
        //since the user is already in login this can never be null
        //for admins ease of viewing the order will be saved under the users name in adminOrders db folder i.e "Orders"
         if (userId!=null && userDetails.value == null){
             viewModelScope.launch {
                 withContext(Dispatchers.IO){
                     val userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)
                     val user = userReference.get().await().getValue(UserRegisterModel::class.java)
                     userDetails.postValue(user)
                     transportRequired = user?.TransportRequired
                     Log.i("TAG","the user details are:"+user.toString())
                 }
             }
         }
         }

    fun ordernow() {

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



        if ((userDetails.value)==null ) {
            //it means he is either an admin or userDetailsData  is not yet received
            //in this case we just tell the customer to wait or check internet connection..
            Log.i("TAG","the outstanding of the customer in null Areyou the admin")
            return
        }
        val order = OrderModel()
        order.userId = userId
        order.orderId = orderId.toString()
        order.userName = name
        order.quantity =(cartItemsList.size).toString()
        order.date = date
        order.orderValue = getCartValue()
        order.cartItemList = cartItemsList



        if ((outstanding!!.toInt())>0){
            //order status -1 means the order is in waiting stage and will have to get acceptance from th admin ...
            order.orderStatus = "-1"
            val retrofit = Retrofit.Builder()
                .baseUrl("https://sureshdairyfoods-f8a5a.web.app/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val api = retrofit.create(Api::class.java)
            val call: Call<ResponseBody> = api.sendNotification("Hii","Woohooo","anthera ayya aipoindiii")
            call.enqueue(object :retrofit2.Callback<ResponseBody>{
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {

                    try {

                   }
                    catch (e: IOException){
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

        //clearing the cart after order has been successfully placed..
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteAll()
        }
    }



    //used for removing the cartItem
    fun removeItem(cartItem: CartItem) {
        viewModelScope.launch {
            repository.delete(cartItem.Name)
        }
    }
}
