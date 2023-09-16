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
            //requestedOrders to AcceptedOrders has been done
            Log.i("UserOrdersAdminview","undo status has been updated")
            if (it == "Undo"){
                Snackbar("Undo Operation Success")
            }
            else{
                SnackbarWithUndo(it)
            }
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

    fun SnackbarWithUndo(newOrderStatus:String){
        val msg = if(newOrderStatus == "0") "Order has been moved to Accepted Orders"
        else "Order has been Dispatched"
        Snackbar.make(binding.userOrdersAdminViewRV,msg,Snackbar.LENGTH_LONG)
            .setAction("UNDO",View.OnClickListener {
                userOrdersAdminViewModel.undoOrderMoving()
            }).addCallback(object : Snackbar.Callback(){
                override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                    super.onDismissed(transientBottomBar, event)
                    userOrdersAdminViewModel.clearUndoOrderDetails()
                }

                override fun onShown(sb: Snackbar?) {
                    super.onShown(sb)
                }
            })
            .show()
    }
    fun Snackbar(msg:String){
        Snackbar.make(binding.userOrdersAdminViewRV,msg, Snackbar.LENGTH_LONG).show()
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

                CoroutineScope(Dispatchers.IO).launch {
                    val task = userOrdersAdminViewModel.swiped(viewHolder.bindingAdapterPosition,direction)
                    if (task == null) {
                        withContext(Dispatchers.Main) {
                            Snackbar("Invalid Action")
                            userOrdersAdminViewAdapter?.notifyItemChanged(viewHolder.bindingAdapterPosition)
                        }
                    }
                    else{
                        delay(4000)
                        //if the task is  unsuccessful with in 4 seconds we will display a snackBar saying Please check your Internet Connection
                        if (!task.isSuccessful){
                            withContext(Dispatchers.Main)
                            {
                                userOrdersAdminViewAdapter?.notifyItemChanged(viewHolder.bindingAdapterPosition)
                                Snackbar("Unsuccessful")
                            }
                        }
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