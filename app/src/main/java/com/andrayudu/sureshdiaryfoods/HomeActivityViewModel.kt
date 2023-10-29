package com.andrayudu.sureshdiaryfoods

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrayudu.sureshdiaryfoods.model.OrderModel
import com.andrayudu.sureshdiaryfoods.model.PaymentModel
import com.andrayudu.sureshdiaryfoods.model.UserRegisterModel
import com.andrayudu.sureshdiaryfoods.utility.Event
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeActivityViewModel(): ViewModel() {


    val tag = "HomeActivityViewModel"

    private val mAuth = FirebaseAuth.getInstance()
    private val mDb   = FirebaseDatabase.getInstance()
    private val userId = mAuth.currentUser?.uid


    //stores the userDetails,used in both home,profilefrags
    private val _userLive = MutableLiveData<UserRegisterModel?>()
    val userLive: LiveData<UserRegisterModel?>
       get() = _userLive

    //ordersfrag Related
    private val _ordersListLive = MutableLiveData<List<OrderModel>>()
    val ordersListLive: LiveData<List<OrderModel>>
       get() = _ordersListLive
    private val customerOrdersList = ArrayList<OrderModel>()
    private val _datesList = ArrayList<Int>()
     val datesList:ArrayList<Int>
       get() = _datesList

    //paymentsFrag Related
    private val paymentsList = ArrayList<PaymentModel>()
    private val _paymentListLive = MutableLiveData<List<PaymentModel>>()
     val paymentListLive : MutableLiveData<List<PaymentModel>>
       get()= _paymentListLive


    //profileFragRelated
    private val _eventNotifyLiveData = MutableLiveData<Event<String>>()
     val eventNotifyLiveData :MutableLiveData<Event<String>>
        get() = _eventNotifyLiveData



    //fetches the outstanding of user...
    //and subscribes the user to appropriate notif channel...
    fun userInit() {

        viewModelScope.launch(Dispatchers.IO) {

         try{

                 val  user = mDb.getReference("UsersTesting").child(userId!!).get()
                    .await().getValue(UserRegisterModel::class.java)
                 _userLive.postValue(user)
                subscribeToSDF()

            } catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    //Admins notif channel,which gets requests from customers..
    private fun subscribeToAdminNotifications() {
        Firebase.messaging.subscribeToTopic("notifications")
            .addOnCompleteListener{task->
                var msg = "Subscribed"
                if (!task.isSuccessful){
                    msg = "Subscribe failed"
                }
                Log.i(tag,"The status of admins Subscription to notifications:${msg}")
            }
    }

    //General notification channel for all customers
    private fun subscribeToSDF() {
        Firebase.messaging.subscribeToTopic("sureshDairyFoods")
            .addOnCompleteListener{task->
                var msg = "Subscribed"
                if (!task.isSuccessful){
                    msg = "Subscribe failed"
                }
                Log.i(tag,"The status of customers Subscription to SDF notifications:${msg}")
            }
    }

    //loads the customer payments related info ...
    fun loadCustomerPayments(){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val customerPayments = mDb.getReference("CustomerPayments").child(userId!!)
                customerPayments.addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        paymentsList.clear()
                        if (snapshot.exists()){
                            for (dataSnapshot in snapshot.children){
                                val payment = dataSnapshot.getValue(PaymentModel::class.java)
                                if (payment != null) {
                                    paymentsList.add(payment)
                                }

                            }
                            _paymentListLive.postValue(paymentsList)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}

                })
            //mostly the catch will run only if the userId is null(which is never going to happen)
            }catch (e:Exception){
                e.printStackTrace()
                Log.e(tag,"there is an exception:"+e.message.toString())
            }
        }

    }



    //loads the customers OrdersData and posts it to ordersListLive...
    fun loadOrdersData(){

        viewModelScope.launch(Dispatchers.IO) {

            val customersordersRef =  mDb.getReference("CustomerOrdersTesting").child(userId!!)
            customersordersRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clearing the arraylist because it will load duplicate values...
                    customerOrdersList.clear()
                    if (snapshot.exists()){
                        for (datasnapshot in snapshot.children){
                            val order = datasnapshot.getValue(OrderModel::class.java)

                            if (order != null) {
                                customerOrdersList.add(order)
                            }
                        }
                        val sortedOrdersList = sortListByDate(customerOrdersList)
                        createDatesList(sortedOrdersList)
                        _ordersListLive.postValue(sortedOrdersList)

                    }
                    //if the orders snap doesnt exist then we will return empty list to the recycler view...
                    else{
                        _ordersListLive.postValue(customerOrdersList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }
    }

    //this function creates a dates list which will be used in  orders RecyclerView...
    private fun createDatesList(sortedCustomerOrdersList: List<OrderModel>) {
        _datesList.clear()
        var prevDate:String? = null
        for (order in sortedCustomerOrdersList) {
            if (prevDate == order.date) {
                _datesList.add(0)
            } else {
                prevDate = order.date
                _datesList.add(1)
            }

        }


    }

    //sorts the customerOrders list by date in descending order...
    private fun sortListByDate(customerOrdersList: ArrayList<OrderModel>): List<OrderModel> {

        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val sortedList = customerOrdersList.sortedByDescending {
            LocalDate.parse(it.date, dateTimeFormatter)
        }
        return sortedList
    }


    fun loadUserData() {

        viewModelScope.launch(Dispatchers.IO) {
            val userReference = FirebaseDatabase.getInstance().getReference("UsersTesting").child(userId!!)
            userReference.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userRegisterModel = snapshot.getValue(UserRegisterModel::class.java)
                        _userLive.postValue(userRegisterModel)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        }
    }

    fun logOut() {
        mAuth.signOut()
        //if the logging out is successful then we will post logout action
        if (mAuth.currentUser == null){
            _eventNotifyLiveData.postValue(Event("Logout"))
        }

    }


}