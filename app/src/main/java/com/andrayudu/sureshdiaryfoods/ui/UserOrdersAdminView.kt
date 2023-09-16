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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
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
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class UserOrdersAdminView : AppCompatActivity() {

    private lateinit var binding:ActivityUserOrdersAdminViewBinding
    private lateinit var userOrdersAdminViewModel: UserOrdersAdminViewModel

    var userOrdersAdminViewAdapter: UserOrdersAdminViewAdapter? = null



    private val orderSpinnerItems = arrayOf("AcceptedOrders","RequestedOrders")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_user_orders_admin_view)
        userOrdersAdminViewModel = ViewModelProvider(this)[UserOrdersAdminViewModel::class.java]

        initRecyclerView()
        initSpinner()

        userOrdersAdminViewModel.loadUserNames()

    }

    private fun initSpinner() {
        val spinnerAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,orderSpinnerItems)
        binding.ordersSpinner.adapter =spinnerAdapter
        binding.ordersSpinner.onItemSelectedListener = spinListener
        binding.ordersSpinner.dropDownVerticalOffset = 100
    }
    private fun loadAcceptedOrders() {
        Log.i("TAG","loading acceptedOrders")
        userOrdersAdminViewAdapter?.setList(userOrdersAdminViewModel.acceptedOrders)
    }
    private fun loadRequestedOrders() {
        Log.i("TAG","loading requestedOrders")
        userOrdersAdminViewAdapter?.setList(userOrdersAdminViewModel.requestedOrders)

    }


    private fun initRecyclerView() {
        binding.userOrdersAdminViewRV.layoutManager = LinearLayoutManager(this)
        userOrdersAdminViewAdapter = UserOrdersAdminViewAdapter { selectedItem: OrderModel? -> listItemClicked(selectedItem!!) }
        binding.userOrdersAdminViewRV.adapter = userOrdersAdminViewAdapter
        itemTouchHelper.attachToRecyclerView(binding.userOrdersAdminViewRV)

        userOrdersAdminViewModel.getUndoStatus().observe(this, Observer {
            Snackbar.make(binding.userOrdersAdminViewRV,it,Snackbar.LENGTH_SHORT).show()
        })
        userOrdersAdminViewModel.getAcceptedOrders().observe(this, Observer {
            if(userOrdersAdminViewModel.getSpinnerPosition() ==0){
                userOrdersAdminViewAdapter!!.setList(it)
            }
        })

        userOrdersAdminViewModel.getRequestedOrders().observe(this, Observer {
            if (userOrdersAdminViewModel.getSpinnerPosition()== 1){
                userOrdersAdminViewAdapter!!.setList(it)
            }
        })


    }

    private fun listItemClicked(orderModel: OrderModel) {

        Toast.makeText(this,"Selected food is ${orderModel.cartItemList}", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, OrderDetails::class.java)
        intent.putExtra("orderModel",orderModel)
        startActivity(intent)

    }
   private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.DOWN,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT){
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            //this method is called when the item is moved like from top to bottom
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {


            val acceptedOrder = "0"
            val dispatchedOrder ="1"
            val requestedOrder ="-1"
            //place not null conditions for both lists coz on swiping fast index out of bounds error coming
            val order = if (userOrdersAdminViewModel.getSpinnerPosition() == 0) userOrdersAdminViewModel.acceptedOrders.get(viewHolder.getBindingAdapterPosition())
            else userOrdersAdminViewModel.requestedOrders.get(viewHolder.getBindingAdapterPosition())

            //this method is called when we swipe our item to left direction
            if (direction == ItemTouchHelper.LEFT)
            {
                //RequestedOrders to AcceptedOrder Moving
                if (order.orderStatus == "-1"){


                    userOrdersAdminViewModel.changeOrderStatus(order.orderId!!,acceptedOrder,viewHolder.bindingAdapterPosition)
                    userOrdersAdminViewAdapter?.deleteItem(viewHolder.bindingAdapterPosition)
                            Log.i("TAG","moving is successfull bro")

//                                userOrdersAdminViewAdapter?.deleteItem(viewHolder.getBindingAdapterPosition())
//                                Snackbar.make(binding.userOrdersAdminViewRV,"${order.orderId} has been moved to accepted Orders",Snackbar.LENGTH_SHORT)
//                                    .setAction("Undo",View.OnClickListener {
//                                       userOrdersAdminViewModel.undoOrderMoving(order.orderId!!,requestedOrder)
//
//                                        Log.i("undoing the action now","to -1")
//                                    }).show()

                            }

                        else{

                                Snackbar.make(
                                    binding.userOrdersAdminViewRV,
                                    "The moving Operation Failed,Please Check your Internet Connection",
                                    Snackbar.LENGTH_SHORT
                                ).show()


                        }



                //tried to move Accepted Order again to Accepted  i.e, order.orderStatus == "0"
            }

            //this method is called if swipe is in RightDirection
            // we will put the order status to "1" ie delivered or dispatched..
            else{
                //if the orders status is "accepted" then only it can be changed to 1 ie delivered/dispatched...
                if ((order.orderStatus).equals("0")) {

                  val moveToDispatched =  FirebaseDatabase.getInstance().getReference("Orders")
                        .child(order.orderId.toString()).child("orderStatus").setValue("1")
                    GlobalScope.launch(Dispatchers.IO) {
                        moveToDispatched.await()
                        if(moveToDispatched.isSuccessful){
                            withContext(Dispatchers.Main){
                                userOrdersAdminViewAdapter?.deleteItem(viewHolder.getBindingAdapterPosition())
                            }
                        }
                    }
                }
                //if an order for which order status is "-1" it cant be set to "1" directly
                //first it should be moved to acceptedOrders
                else{
                    userOrdersAdminViewAdapter?.notifyItemChanged(viewHolder.getBindingAdapterPosition())
                    Snackbar.make(binding.userOrdersAdminViewRV,"Please Accept the Order First:",Snackbar.LENGTH_SHORT).show()
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
   private val spinListener:AdapterView.OnItemSelectedListener = object :AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (position == 0){
                userOrdersAdminViewModel.setSpinnerPosition( position)
                loadAcceptedOrders()
            }
            else{
                userOrdersAdminViewModel.setSpinnerPosition( position)
                loadRequestedOrders()
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }


    }

}