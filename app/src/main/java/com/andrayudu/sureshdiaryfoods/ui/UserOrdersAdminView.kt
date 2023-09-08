package com.andrayudu.sureshdiaryfoods.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.adapters.OrdersAdapter
import com.andrayudu.sureshdiaryfoods.adapters.UserOrdersAdminViewAdapter
import com.andrayudu.sureshdiaryfoods.databinding.ActivityUserOrdersAdminViewBinding
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.model.OrderModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class UserOrdersAdminView : AppCompatActivity() {

    private lateinit var binding:ActivityUserOrdersAdminViewBinding

    var userOrdersAdminViewAdapter: UserOrdersAdminViewAdapter? = null
    var customerNamesList = ArrayList<OrderModel>()
    var acceptedOrders = ArrayList<OrderModel>()
    var requestedOrders = ArrayList<OrderModel>()
    val orderSpinnerItems = arrayOf("AcceptedOrders","RequestedOrders")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_user_orders_admin_view)


        initRecyclerView()

        val spinnerAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_item,orderSpinnerItems)
        binding.ordersSpinner.adapter =spinnerAdapter
        binding.ordersSpinner.onItemSelectedListener = spinListener

        getUserNames()



    }
    val spinListener:AdapterView.OnItemSelectedListener = object :AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (position == 0){
                loadAcceptedOrders()
            }
            else{
                loadRequestedOrders()
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }


    }
    private fun loadAcceptedOrders() {
        Log.i("TAG","loading acceptedOrders")
        userOrdersAdminViewAdapter?.setList(acceptedOrders)

    }
    private fun loadRequestedOrders() {
        Log.i("TAG","loading requestedOrders")
        userOrdersAdminViewAdapter?.setList(requestedOrders)

    }


    private fun initRecyclerView() {
        binding.userOrdersAdminViewRV.layoutManager = LinearLayoutManager(this)
        userOrdersAdminViewAdapter = UserOrdersAdminViewAdapter { selectedItem: OrderModel? -> listItemClicked(selectedItem!!) }
        binding.userOrdersAdminViewRV.adapter = userOrdersAdminViewAdapter
    }

    private fun listItemClicked(orderModel: OrderModel) {

        Toast.makeText(this,"Selected food is ${orderModel.cartItemList}", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, OrderDetails::class.java)
        intent.putExtra("orderModel",orderModel)
        startActivity(intent)

    }

    private fun getUserNames() {
        val adminOrdersRef = FirebaseDatabase.getInstance().getReference("Orders")
        adminOrdersRef.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                requestedOrders.clear()
                acceptedOrders.clear()
                if (snapshot.exists()){
                    for (dataSnapshot in snapshot.children){
                        val order:OrderModel? = dataSnapshot.getValue(OrderModel::class.java)
                        if (order!!.orderStatus == "0"){
                            acceptedOrders.add(order)
                        }
                        else{
                            requestedOrders.add(order)
                        }
                        //customerNamesList contains both accepted and requestes orders
                        customerNamesList.add(order)
                    }

                    userOrdersAdminViewAdapter?.setList(customerNamesList)
                    Log.i("TAG","the customer names list is:"+customerNamesList.toString())
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })
    }
}