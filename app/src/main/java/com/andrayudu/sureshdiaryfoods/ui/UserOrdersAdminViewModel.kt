package com.andrayudu.sureshdiaryfoods.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrayudu.sureshdiaryfoods.model.OrderModel
import com.bumptech.glide.load.resource.bitmap.FitCenter
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class UserOrdersAdminViewModel():ViewModel() {

    val TAG = "UserOrdersAdminViewModel"

    private var spinnerPosition = 0
    private val ORDERSTATUS = "orderStatus"
    private val Orders = "Orders"
    private val acceptedOrdersLive = MutableLiveData<List<OrderModel>>()
    private val requestedOrdersLive = MutableLiveData<List<OrderModel>>()
    private val undoStatus = MutableLiveData<String>()

    var customerOrdersList = ArrayList<OrderModel>()
    var acceptedOrders = ArrayList<OrderModel>()
    var requestedOrders = ArrayList<OrderModel>()
    var datesList = ArrayList<Int>()
    var undoCustomerId:String? = null
    var undoOrderId:String? = null
    var undoOrderStatus:String? = null

    private val firebaseRef = FirebaseDatabase.getInstance()



    fun getSpinnerPosition():Int{
        return spinnerPosition
    }
    fun setSpinnerPosition(spinnerPosition:Int){
        this.spinnerPosition = spinnerPosition

    }


    fun getAcceptedOrders(): LiveData<List<OrderModel>> {
        return acceptedOrdersLive
    }

    fun getRequestedOrders(): LiveData<List<OrderModel>> {
        return requestedOrdersLive
    }

    fun getUndoStatus(): LiveData<String> {
        return undoStatus
    }

    //After Snackbar disappears this will be called to clear the previous order Details...
    fun clearUndoOrderDetails(){
        undoOrderId  = null
        undoOrderStatus = null
        Log.i(TAG,"undoOrder Details have been cleared")
    }

    fun swiped(position:Int,direction:Int):Task<Void>?{

        val left = 4
        val right = 8
        val order = if (getSpinnerPosition() == 0) acceptedOrders.get(position)
        else requestedOrders.get(position)

        var changeTask:Task<Void>? = null

        val orderId = order.orderId
        val orderStatus = order.orderStatus
        val customerUid = order.userId
        val changeLocation  =  firebaseRef.getReference(Orders)
            .child(orderId!!)
        val customerDbChangeLocation = firebaseRef.getReference("CustomerOrders").child(customerUid!!).child(orderId)

        val newStatus: String

        //requested to Accepted
        if (orderStatus == "-1" && direction == left ) {
            newStatus = "0"
            changeTask =changeLocation.child(ORDERSTATUS).setValue(newStatus)
            val customerDbTask =  customerDbChangeLocation.child(ORDERSTATUS).setValue(newStatus)
            Log.i("UserOrdersAdminviewModel", "move the order with orderId to acceptedOrders" + orderId)
            changeTask.addOnSuccessListener {
                customerDbTask.addOnSuccessListener {
                    undoStatus.postValue("0")
                    undoCustomerId = customerUid
                    undoOrderId = orderId
                    undoOrderStatus = "-1"
                }

            }
        }
        //Accepted to Dispatched
        else if(orderStatus == "0" && direction == right){
            newStatus ="1"
            changeTask = changeLocation.child(ORDERSTATUS).setValue(newStatus)
            val customerDbTask = customerDbChangeLocation.child(ORDERSTATUS).setValue(newStatus)
            Log.i("UserOrdersAdminviewModel", "move the order with orderId to DispatchedOrders" + orderId)
            changeTask.addOnSuccessListener {
                customerDbTask.addOnSuccessListener {
                    undoStatus.postValue("1")
                    undoCustomerId = customerUid
                    undoOrderId = orderId
                    undoOrderStatus = "0"
                }

            }
        }
        //below line will return null if swiped right from Requested Orders and left from AcceptedOrders as the action is invalid
        return changeTask
}



        fun undoOrderMoving() {
            if (undoOrderId!=null && undoOrderStatus!=null){
                viewModelScope.launch(Dispatchers.IO) {
                    val task = firebaseRef.getReference(Orders)
                        .child(undoOrderId!!).child(ORDERSTATUS).setValue(undoOrderStatus)
                    val customerDbUndoTask = firebaseRef.getReference("CustomerOrders").child(undoCustomerId!!).child(undoOrderId!!)
                        .child(ORDERSTATUS).setValue(undoOrderStatus)
                    task.await()
                    customerDbUndoTask.await()
                    if (task.isSuccessful && customerDbUndoTask.isSuccessful){
                        undoStatus.postValue("Undo")
                    }
                }
            }


        }

    //gets the names of users whose orders are currently in "Orders" db folder
    fun loadCustomerOrders() {
        val adminOrdersRef = FirebaseDatabase.getInstance().getReference("Orders")
        adminOrdersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                requestedOrders.clear()
                acceptedOrders.clear()
                customerOrdersList.clear()

                Log.i("UserOrdersAdminviewModel","Firebase Data is being Loaded")
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val order: OrderModel? = dataSnapshot.getValue(OrderModel::class.java)
                        if (order!!.orderStatus == "0") {
                            acceptedOrders.add(order)
                        } else if (order.orderStatus == "-1") {
                            requestedOrders.add(order)
                        }
                        //customerNamesList contains both accepted and requested orders
                        customerOrdersList.add(order)

                    }
                    acceptedOrdersLive.postValue(acceptedOrders)
                    requestedOrdersLive.postValue(requestedOrders)


                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }




}
