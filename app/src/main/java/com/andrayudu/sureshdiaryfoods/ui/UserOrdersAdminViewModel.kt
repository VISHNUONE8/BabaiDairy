package com.andrayudu.sureshdiaryfoods.ui

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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


    private var spinnerPosition = 0
    private val ORDERSTATUS = "orderStatus"
    private val Orders = "Orders"
    private val acceptedOrdersLive = MutableLiveData<List<OrderModel>>()
    private val requestedOrdersLive = MutableLiveData<List<OrderModel>>()
    private val undoStatus = MutableLiveData<String>()
    var customerNamesList = ArrayList<OrderModel>()
    var acceptedOrders = ArrayList<OrderModel>()
    var requestedOrders = ArrayList<OrderModel>()


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


    fun changeOrderStatus(orderId: String, status: String, position: Int) {

        viewModelScope.launch(Dispatchers.IO) {
            val change = FirebaseDatabase.getInstance().getReference(Orders)
                .child(orderId).child(ORDERSTATUS).setValue(status)
            change.await()
            Log.i("TAG", "move the order with orderId to acceptedOrders" + orderId)

                undoStatus.postValue(position.toString())

        }
    }

        fun undoOrderMoving(orderId: String, status: String) {
            viewModelScope.launch(Dispatchers.IO) {
                val task = FirebaseDatabase.getInstance().getReference(Orders)
                    .child(orderId).child(ORDERSTATUS).setValue(status)
                task.await()
                undoStatus.postValue("Undo Operation is Successful")
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
                if (snapshot.exists()) {
                    for (dataSnapshot in snapshot.children) {
                        val order: OrderModel? = dataSnapshot.getValue(OrderModel::class.java)
                        if (order!!.orderStatus == "0") {
                            acceptedOrders.add(order)
                            acceptedOrdersLive.postValue(acceptedOrders)
                        } else if (order.orderStatus == "-1") {
                            requestedOrders.add(order)
                            requestedOrdersLive.postValue(requestedOrders)
                        }
                        //customerNamesList contains both accepted and requested orders
                        customerNamesList.add(order)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }




}
