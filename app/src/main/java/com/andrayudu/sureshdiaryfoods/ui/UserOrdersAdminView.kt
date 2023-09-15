package com.andrayudu.sureshdiaryfoods.ui

import android.content.Intent
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.adapters.OrdersAdapter
import com.andrayudu.sureshdiaryfoods.adapters.UserOrdersAdminViewAdapter
import com.andrayudu.sureshdiaryfoods.databinding.ActivityUserOrdersAdminViewBinding
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.model.OrderModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

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
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                //this method is called when the item is moved like from top to bottom
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {


                //this method is called when we swipe our item to left direction
                if (direction == ItemTouchHelper.LEFT)
                {

                    if (requestedOrders.isEmpty()){
                        Log.i("TAG","the requested Orders is empty")
                        //the below snippet is used because on swiping the item, it is going to hiding position
                        //to bring it back to normal we are using below snippet
                        userOrdersAdminViewAdapter!!.notifyItemChanged(viewHolder.adapterPosition)
                    }
                    else {


                        //we will just have to change the status of order from RequestedOrders list from -1 to 0
                        val order = requestedOrders.get(viewHolder.adapterPosition)
                        //if the order is already an accepted one and still swiped left then
                        if (order.orderStatus == "0") {
                            Toast.makeText(
                                this@UserOrdersAdminView,
                                "This order is already in Accepted List",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            FirebaseDatabase.getInstance().getReference("Orders")
                                .child(order.orderId.toString()).child("orderStatus").setValue("0")
                            Log.i(
                                "TAG",
                                "move the order with orderId to acceptedOrders" + order.orderId
                            )
//                   userOrdersAdminViewAdapter?.deleteItem(viewHolder.adapterPosition)

                            Snackbar.make(binding.userOrdersAdminViewRV,"${order.orderId} has been moved to accepted Orders",Snackbar.LENGTH_SHORT).setAction("Undo",View.OnClickListener {
                                FirebaseDatabase.getInstance().getReference("Orders")
                                    .child(order.orderId.toString()).child("orderStatus").setValue("-1")
                                Log.i("undoing the action now","to -1")
                            }).show()
                        }
                    }


                }
                //if the direction is right then we will put the order status to "1" ie delivered or dispatched..
                else{
                    val order = acceptedOrders.get(viewHolder.adapterPosition)
                    //if the orders status is "accepted" then only it can be changed to 1 ie delivered/dispatched...
                    if ((order.orderStatus).equals("0")) {
                        Log.i(
                            "TAG",
                            "move the order with orderId to delivered" + order.orderId
                        )
                        FirebaseDatabase.getInstance().getReference("Orders")
                            .child(order.orderId.toString()).child("orderStatus").setValue("1")
                    }
                    //if an order for which deliver status is "-1" it cant be set to "1" directly
                    //first it should be moved to acceptedOrders
                    else{
                        Toast.makeText(this@UserOrdersAdminView,"The order is in Request State,Swipe Left to move it into the Accepted Orders....",Toast.LENGTH_SHORT).show()
                    }

                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                RecyclerViewSwipeDecorator.Builder(c,recyclerView,viewHolder,dX,dY,actionState,isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(this@UserOrdersAdminView,R.color.colorPrimary))
                    .addSwipeLeftActionIcon(R.drawable.baseline_handshake_24)
                    .addSwipeLeftLabel("Accept")
                    .addSwipeRightLabel("Dispatched")
                    .addSwipeRightBackgroundColor((ContextCompat.getColor(this@UserOrdersAdminView,R.color.colorPrimaryDark)))
                    .addSwipeRightActionIcon(R.drawable.baseline_done_outline_24)
                    .create()
                    .decorate()
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.userOrdersAdminViewRV)
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
                        else if (order.orderStatus =="-1"){
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