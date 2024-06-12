package com.andrayudu.sureshdiaryfoods.ui

import android.util.Log
import android.widget.Toast
import androidx.lifecycle.*
import com.andrayudu.sureshdiaryfoods.Api
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.model.OrderModel
import com.andrayudu.sureshdiaryfoods.model.UserRegisterModel
import com.andrayudu.sureshdiaryfoods.utility.RetrofitClientInstance
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.abs

class CartViewModel(private val repository: CartItemRepository):ViewModel() {

    private val TAG = "CartViewModel"

    //Firebase
    private val mDb = FirebaseDatabase.getInstance()
    private val mAuth = FirebaseAuth.getInstance()
    private val userId = mAuth.currentUser?.uid

    private val repo = repository
    private val cartItems = repository.cartItems

    private var user: UserRegisterModel? = null
    private var cartItemsList: List<CartItem>? = null
    private var cartValue: Int = 0
    private var orderTransportCost: Int = 0
    var userTransportCharges: Int = 0

    //LiveData
    private val grandTotal = MutableLiveData<String?>()
    private val transportChargesLive = MutableLiveData<String?>()
    private val statusLive = MutableLiveData<String?>()
    private val userDetails = MutableLiveData<UserRegisterModel?>()


    fun getCartValue(): Int {
        return cartValue
    }

    fun getStatusLive(): LiveData<String?> {
        return statusLive
    }

    fun getTransportValue(): String {
        return orderTransportCost.toString()
    }

    //this function calculates the total cost of items in the cart(without transport)
    fun cartItemsCost() {

        cartItemsList = cartItems.value

        if (cartItemsList != null) {
            //this list will be uploaded to firebase soo we have to keep it up-to-date

            cartValue = 0
            for (cartItem in cartItemsList!!) {
                if (cartItem.Category.equals("Kova") || cartItem.Category.equals("KovaSpl")) {
                    cartValue =
                        (cartValue + (cartItem.Price * cartItem.Quantity * 3))
                } else {
                    cartValue =
                        (cartValue + (cartItem.Price * cartItem.Quantity))
                }
            }

        }
        //here we are calculating the Transport charges for a customer...
        viewModelScope.launch(Dispatchers.IO) {

            //if the user has transport enabled
            if (userTransportCharges > 0) {
                var kovaCount = 0
                //kovaCountFromDb uses default context i.e Dispatchers.IO used above in viewmodelscope
                val kovaCountFromDbTask = launch {
                    kovaCount = repository.getKovaCount()
                }
                kovaCountFromDbTask.join()
                Log.i(TAG, "kova category count is calculated$kovaCount")
                if (kovaCountFromDbTask.isCompleted) {
                    orderTransportCost = (userTransportCharges * kovaCount)
                    Log.i(TAG, "the transport charges for customer are:${orderTransportCost}")
                    transportChargesLive.postValue(orderTransportCost.toString())
                }

            }
            //if the user has transport disabled i.e 0
            else {
                transportChargesLive.postValue(orderTransportCost.toString())
            }
            grandTotal.postValue((cartValue + (orderTransportCost)).toString())

        }
    }

    fun getCartItems(): LiveData<List<CartItem>> {
        return cartItems
    }

    fun getGrandTotal(): LiveData<String?> {
        return grandTotal
    }

    // this fun is used to get the customers details like name,outstanding balance,mainly for userTransportCharges...
    fun getUserDetails(): LiveData<UserRegisterModel?> {
        //since the user is already in login this can never be null
        //for admins ease of viewing the order will be saved under the users name in adminOrders db folder i.e "Orders"
        if (userId != null && userDetails.value == null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val userReference = mDb.getReference("UsersTesting").child(userId!!)
                    user = userReference.get().await().getValue(UserRegisterModel::class.java)
                    user?.let {
                        userDetails.postValue(user)
                        userTransportCharges = user!!.TransportCharges
                        Log.i(TAG, "the transport charges are:$userTransportCharges")
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "the exception is:${e.message.toString()}")

                }
            }
        }
        return userDetails
    }

    //created orderId using system timeinMillis+"random number from 1 to 1000"
    private fun createOrderId(): String {
        return StringBuilder()
            .append(System.currentTimeMillis())
            .append(abs(Random().nextInt(1000)))
            .toString()
    }

    //starts the ordering procedure
    fun placeOrder() {


        val orderId = createOrderId()
        val date = getDate()


        try {
            //this data is used only for posting Orders
            val name = user!!.Name
            val outstanding = user!!.Outstanding
            val limit = user!!.Limit

            //orderDetails
            val order = OrderModel()
            order.userId = userId
            order.orderId = orderId
            order.userName = name
            order.quantity = (cartItemsList?.size)!!
            order.date = date
            order.orderValue = getCartValue()
            order.transportCharges = userTransportCharges
            order.grandTotal = grandTotal.value!!.toInt()
            order.cartItemList = cartItemsList


            viewModelScope.launch {


                //if the account status of the user is in hold ,then we will not proceed further with the order...
                //this is useful for publishing updatess...
                if (user!!.onHold) {
                    statusLive.postValue("Hold")
                    return@launch
                }

//                //if the limit exceeds then we will not proceed with the order
//                else if (order.orderValue > (limit)) {
//                    Log.i(TAG, "The order value is exceeding the usersLimit")
//                    statusLive.postValue("Limit")
//                    return@launch
//                }

//                // outstanding is -ve balancethen we will place the order in hold state ie -1
//                else if ((outstanding) <  0) {
//                    //order status -1 means the order is in waiting stage and will have to get acceptance from th admin ...
//                    order.orderStatus = -1
//                    sendNotifToAdmin(order)
//                }
                else {

                    //orderstatus 0 implies that the orderplaced succesfully
                    order.orderStatus = 0

                }

                createRazorpayOrder()
                updateToDb(order)


            }

        } catch (e: Exception) {
            Log.e("TAG", "the error is:${e.message.toString()}")
        }


    }

    //updates the order details to both customerOrders and Orders db
    private suspend fun updateToDb(order: OrderModel) {

        val orderId = order.orderId

        withContext(Dispatchers.IO) {
            //customerOrders is the db reference which is used for customers
            val customerOrdersRef = mDb.getReference("CustomerOrdersTesting").child(userId!!)
            //Orders is the db reference which is used for Admin
            val adminOrdersRef = mDb.getReference("OrdersTesting")


            val customerDbTask = customerOrdersRef.child(orderId!!).setValue(order)
            val adminDbTask = adminOrdersRef.child(orderId).setValue(order)

            customerDbTask.await()
            adminDbTask.await()
            //clearing the cart after order has been successfully placed and
            // also tell the user that the order has been successfully placed
            if (customerDbTask.isSuccessful && adminDbTask.isSuccessful) {

                statusLive.postValue("Success")
                repo.deleteAll()
            }
        }

    }

    private fun createRazorpayOrder() {
        viewModelScope.launch {
            val merchantOrderId = createOrderId()
            val service =
                RetrofitClientInstance.retrofitInstance!!.create(Api::class.java)
            val response = service.createOrder(merchantOrderId, 1000.0)
            if (response.isSuccessful) {
                val body = response.body()
                val rzpOrderId = body?.get("rzp_order_id")
                val rzpId = body?.get("rzp_id")
                if (rzpId != null && rzpOrderId != null) {
                    Log.i(
                        TAG,
                        "generateOrderId: Razorpay order id: $rzpOrderId || Razorpay Id: $rzpId"
                    )
//                    makePayment(
//                        merchantOrderId,
//                        rzpOrderId.toString(),
//                        rzpId.toString(),
//                        amount
//                    )
                } else {
                    Log.i("TAG", "Failed to generate order id")

                }
            } else {
                Log.i("TAG", "Failed to generate order id")
            }
        }
    }


    private fun sendNotifToAdmin(order: OrderModel) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://sureshdairyfoods-f8a5a.web.app/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val api = retrofit.create(Api::class.java)
        val call: Call<ResponseBody> = api.sendNotification(
            "edhookati",
            "Alert",
            "Mr.${order.userName} is requesting you to Accept an Order..."
        )
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {

                try {

                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.i(TAG, "Sorry bro api call failed,Reason:${t.message.toString()}")
            }
        })
    }

    fun clearCart() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteAll()
            statusLive.postValue("Deleted")
        }

    }

    private fun getDate(): String? {
        val current = LocalDateTime.now()
        val dateformatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val date = current.format(dateformatter)
        return date
    }


    //used for removing the cartItem
    fun removeItem(cartItem: CartItem) {
        viewModelScope.launch {
            repository.delete(cartItem.Name)
        }
    }

    //enables firebasecloudmessaging by default it is enabled only , but we are making sure once again as
    //we need to send notification to admin incase of outstanding amount value exists...
    fun runtimeEnableAutoInit() {
        viewModelScope.launch(Dispatchers.IO) {
            // [START fcm_runtime_enable_auto_init]
            Firebase.messaging.isAutoInitEnabled = true
            // [END fcm_runtime_enable_auto_init]
        }

    }
}


class CartViewModelFactory(private val repository: CartItemRepository): ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>):T{
        if(modelClass.isAssignableFrom(CartViewModel::class.java)){
            return CartViewModel(repository) as T
        }
        throw IllegalAccessException("Unknown ViewModel Class")
    }

}