package com.andrayudu.sureshdiaryfoods.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.test.core.app.takeScreenshot
import com.andrayudu.sureshdiaryfoods.model.OrderModel
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
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
    private val task = MutableLiveData<String>()

    var customerNamesList = ArrayList<OrderModel>()
    var acceptedOrders = ArrayList<OrderModel>()
    var requestedOrders = ArrayList<OrderModel>()
    var undoOrderId:String? = null
    var undoOrderStatus:String? = null


    fun getSpinnerPosition():Int{
        return spinnerPosition
    }
    fun setSpinnerPosition(spinnerPosition:Int){
        this.spinnerPosition = spinnerPosition

    }


    fun getAcceptedOrders(): MutableLiveData<List<OrderModel>> {
        return acceptedOrdersLive
    }

    fun getRequestedOrders(): MutableLiveData<List<OrderModel>> {
        return requestedOrdersLive
    }

    fun getUndoStatus(): MutableLiveData<String> {
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
        val changeLocation  =  FirebaseDatabase.getInstance().getReference(Orders)
            .child(orderId!!)
        val newStatus: String

        //requested to Accepted
        if (orderStatus == "-1" && direction == left ) {
            newStatus = "0"
            changeTask =changeLocation.child(ORDERSTATUS).setValue(newStatus)
            Log.i("UserOrdersAdminviewModel", "move the order with orderId to acceptedOrders" + orderId)
            changeTask.addOnSuccessListener {
                undoStatus.postValue("0")
                undoOrderId = orderId
                undoOrderStatus = "-1"


            }
        }
        //Accepted to Dispatched
        else if(orderStatus == "0" && direction == right){
            newStatus ="1"
            changeTask = changeLocation.child(ORDERSTATUS).setValue(newStatus)
            Log.i("UserOrdersAdminviewModel", "move the order with orderId to DispatchedOrders" + orderId)
            changeTask.addOnSuccessListener {
                undoStatus.postValue("1")
                undoOrderId = orderId
                undoOrderStatus = "0"
            }
        }
        //below line will return null if swiped right from Requested Orders and left from AcceptedOrders as the action is invalid
        return changeTask
}



        fun undoOrderMoving() {
            if (undoOrderId!=null && undoOrderStatus!=null){
                viewModelScope.launch(Dispatchers.IO) {
                    val task = FirebaseDatabase.getInstance().getReference(Orders)
                        .child(undoOrderId!!).child(ORDERSTATUS).setValue(undoOrderStatus)
                    task.await()
                    if (task.isSuccessful){
                        undoStatus.postValue("Undo")
                    }
                }
            }


        }

    //gets the names of users whose orders are currently in "Orders" db folder
    fun loadUserNames() {
        val adminOrdersRef = FirebaseDatabase.getInstance().getReference("Orders")
        adminOrdersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                requestedOrders.clear()
                acceptedOrders.clear()
                customerNamesList.clear()

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
                        customerNamesList.add(order)

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
