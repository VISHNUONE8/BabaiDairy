package com.andrayudu.sureshdiaryfoods.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrayudu.sureshdiaryfoods.model.*
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HomeActivityViewModel(): ViewModel() {


    val TAG = "HomeActivityViewModel"

    //Firebase related
    private val mAuth = FirebaseAuth.getInstance()
    private val mDb   = FirebaseDatabase.getInstance()
    private val userId = mAuth.currentUser?.uid



    //this variable is for knowing itemsCatalogue is loaded or not...
     var isLoaded:Boolean = false

    //stores the userDetails
    // used in both home,profilefrags
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

    //ItemsBrochureRelated
    var itemsCatalogueModel = ItemsCatalogueModel()
    private val _itemsCatalogueLive = MutableLiveData<ItemsCatalogueModel>()
    val itemsCatalogueLive : LiveData<ItemsCatalogueModel>
        get()= _itemsCatalogueLive

    //paymentsFrag Related
    private val paymentsList = ArrayList<PaymentModel>()
    private val _paymentListLive = MutableLiveData<List<PaymentModel>>()
     val paymentListLive : LiveData<List<PaymentModel>>
       get()= _paymentListLive


    //profileFragRelated,used for Logout
    private val _eventNotifyLiveData = MutableLiveData<Event<String>>()
     val eventNotifyLiveData :MutableLiveData<Event<String>>
        get() = _eventNotifyLiveData


    //General notification channel for all customers
    //once the app is opened ,we make sure the customer is subscribed to this channel,it will run only once in the app lifecycle..
     fun subscribeToSDF() {
        viewModelScope.launch(Dispatchers.IO) {
            try{

                Firebase.messaging.subscribeToTopic("sureshDairyFoods")
                    .addOnCompleteListener{task->
                        var msg = "Subscribed"
                        if (!task.isSuccessful){
                            msg = "Subscribe failed"
                        }
                        Log.i(TAG,"The status of customers Subscription to SDF notifications:${msg}")
                    }

            } catch (e:Exception){
                e.printStackTrace()
            }
        }

    }

    fun callLoadCustomerPayments(){
        loadCustomerPayments(userId)
    }
    fun callLoadOrdersData(){
        loadCustomerOrders(userId)
    }
    fun callLoadUserData(){
        loadUserData(userId)
    }

    fun loadItemsCatalogue(){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val customerPayments = mDb.getReference("ItemsCatalogue")
                customerPayments.addValueEventListener(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        itemsCatalogueModel = ItemsCatalogueModel()
                        if (snapshot.exists()){

                                val itemsCatalogueFromDb = snapshot.getValue(ItemsCatalogueModel::class.java)
                                if (itemsCatalogueFromDb != null) {
                                    itemsCatalogueModel = itemsCatalogueFromDb
                                }
                            }
                            isLoaded = true
                            _itemsCatalogueLive.postValue(itemsCatalogueModel)
                        }

                    override fun onCancelled(error: DatabaseError) {}

                })
                //mostly the catch will run only if the userId is null(which is never going to happen)
            }catch (e:Exception){
                e.printStackTrace()
                Log.e(TAG,"there is an exception:"+e.message.toString())
            }
        }

    }
    //loads the customer payments related info ...


    fun loadCustomerPayments(usersId:String?){
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val customerPayments = mDb.getReference("CustomerPayments").child(usersId!!)
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
                Log.e(TAG,"there is an exception:"+e.message.toString())
            }
        }

    }

    //loads the customers OrdersData and posts it to ordersListLive...
    fun loadCustomerOrders(usersId:String?){

        viewModelScope.launch(Dispatchers.IO) {

            val customersOrdersRef =  mDb.getReference("CustomerOrdersTesting").child(usersId!!)
            customersOrdersRef.addValueEventListener(object : ValueEventListener {
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
                        val sortedOrdersList = sortOrdersByDate(customerOrdersList)
                        createDatesListLogicRV(sortedOrdersList)
                        _ordersListLive.postValue(sortedOrdersList)

                    }
                    //if the orders snap doesnt exist then we will return empty list to the recycler view...
                    else{
                        _ordersListLive.postValue(customerOrdersList)
                    }
                }
                override fun onCancelled(error: DatabaseError) {}

            })
        }
    }

    //loads the userData and posts it as Live
    //used  in profileFrag,HomeFrag
    fun loadUserData(usersId: String?) {

        try{
            viewModelScope.launch(Dispatchers.IO) {
                val userReference = mDb.getReference("UsersTesting").child(usersId!!)
                userReference.addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val userRegisterModel = snapshot.getValue(UserRegisterModel::class.java)
                            if(userRegisterModel!=null){
                                _userLive.postValue(userRegisterModel)
                            }
                        }
                    }
                    override fun onCancelled(error: DatabaseError) {}
                })
            }

        }catch (e:Exception){
            Log.i(TAG,"An Exception Occurred:${e.message.toString()}")
        }
    }

    //this function creates a 0,1 list from date wise sorted orderslist
    // which will be used in  orders RecyclerView...
     fun createDatesListLogicRV(sortedCustomerOrdersList: List<OrderModel>) {
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
     fun sortOrdersByDate(customerOrdersList: List<OrderModel>): List<OrderModel> {

        val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val sortedList = customerOrdersList.sortedByDescending {
            LocalDate.parse(it.date, dateTimeFormatter)
        }
        return sortedList
    }


    fun logOut() {
        mAuth.signOut()
        //if the logging out is successful then we will post logout action
        if (mAuth.currentUser == null){
            _eventNotifyLiveData.postValue(Event("Logout"))
        }
    }
}