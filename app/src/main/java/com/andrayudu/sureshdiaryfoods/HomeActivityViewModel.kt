package com.andrayudu.sureshdiaryfoods

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.model.OrderModel
import com.andrayudu.sureshdiaryfoods.model.UserRegisterModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeActivityViewModel(): ViewModel() {


    val tag = "HomeActivityViewModel"

    private val mAuth = FirebaseAuth.getInstance()
    private val userId = mAuth.currentUser?.uid
    private var user:UserRegisterModel? = null
    private val userLive = MutableLiveData<UserRegisterModel?>()
    private val orderListLive = MutableLiveData<List<OrderModel>>()


    private val customerOrdersList = ArrayList<OrderModel>()
    private val datesList = ArrayList<Int>()




    //used in OrdersFrag
    fun getOrdersListLive(): LiveData<List<OrderModel>> {
        return orderListLive
    }
    //used in OrdersFragment
    fun getDatesList():ArrayList<Int>{
        return datesList
    }

    fun getUserLive():LiveData<UserRegisterModel?>{
        return userLive
    }


    //gets the user details and knows whether its an admin or user
    //and subscribes the user to appropriate notif channel...
    fun userOrAdmin() {

        viewModelScope.launch(Dispatchers.IO) {
            if (userId != null && user == null) {
                 user = FirebaseDatabase.getInstance().getReference("Users").child(userId).get()
                    .await().getValue(UserRegisterModel::class.java)
                userLive.postValue(user)
                if (user?.role.equals("Admin")) {
                    Log.i(tag, "the user logged in is an Admin..")
                    subscribeToAdminNotifications()
                }
                else {
                    Log.i(tag, "the user logged in is a customer..")
                    subscribeToSDF()
                }
            }
            else{
                userLive.postValue(user)
                Log.i(tag,"user details have been already loaded")
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



    //loads the customers OrdersData and posts it to ordersListLive...
    fun loadOrdersData(){

        viewModelScope.launch(Dispatchers.IO) {

            val customersordersRef =  FirebaseDatabase.getInstance().getReference("CustomerOrders").child(userId!!)
            //getting customer orders to the customerorders list

            customersordersRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clearing the arraylist because it will load duplicate values...
                    customerOrdersList.clear()
                    datesList.clear()
                    var prevDate:String? = null
                    if (snapshot.exists()){
                        for (datasnapshot in snapshot.children){
                            val order = datasnapshot.getValue(OrderModel::class.java)
                            if (prevDate == order?.date){
                                datesList.add(0)
                            }
                            else{
                                prevDate = order?.date
                                datesList.add(1)
                            }
                            customerOrdersList.add(order!!)
                        }
                        orderListLive.postValue(customerOrdersList)
                    }
                    else{
                        orderListLive.postValue(customerOrdersList)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
        }
    }





}