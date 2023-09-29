package com.andrayudu.sureshdiaryfoods.ui

import android.content.Intent
import android.graphics.Canvas
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.adapters.UserOrdersAdminViewAdapter
import com.andrayudu.sureshdiaryfoods.databinding.ActivityUserOrdersAdminViewBinding
import com.andrayudu.sureshdiaryfoods.model.OrderModel
import com.google.android.material.snackbar.Snackbar
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import kotlinx.coroutines.*

class UserOrdersAdminView : AppCompatActivity() {

    private val tag = "UserOrdersAdminView"
    private lateinit var binding:ActivityUserOrdersAdminViewBinding
    private lateinit var userOrdersAdminViewModel: UserOrdersAdminViewModel
    private lateinit var actionBarBackButton: ImageView
    private lateinit var actionBarTextView: TextView

    var userOrdersAdminViewAdapter: UserOrdersAdminViewAdapter? = null
    private val orderSpinnerItems = arrayOf("AcceptedOrders","RequestedOrders")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_user_orders_admin_view)
        userOrdersAdminViewModel = ViewModelProvider(this)[UserOrdersAdminViewModel::class.java]

        actionBarBackButton = binding.actionbarCustomerOrders.findViewById(R.id.actionbar_Back)
        actionBarTextView = binding.actionbarCustomerOrders.findViewById(R.id.actionbar_Text)
        actionBarTextView.text = "Customer Orders"




        initRecyclerView()
        initSpinner()
        initClickListeners()

        userOrdersAdminViewModel.loadCustomerOrders()

    }

    private fun initClickListeners() {
        actionBarBackButton.setOnClickListener {
            onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
                override fun handleOnBackPressed() {

                    finish()
                }
            })
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun initSpinner() {
        val spinnerAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,orderSpinnerItems)
        binding.ordersSpinner.adapter =spinnerAdapter
        binding.ordersSpinner.onItemSelectedListener = spinListener
        binding.ordersSpinner.dropDownVerticalOffset = 100
    }
    private fun loadAcceptedOrders() {
        Log.i("TAG","loading acceptedOrders")
        val acceptedOrders = userOrdersAdminViewModel.acceptedOrders
        if (acceptedOrders.isEmpty()){
            binding.userOrdersAdminViewRV.visibility = View.GONE
            binding.noItemsIndicator.text = "No Accepted Orders For now..."
            return
        }
        binding.userOrdersAdminViewRV.visibility = View.VISIBLE
        userOrdersAdminViewAdapter?.setList(acceptedOrders)
    }
    private fun loadRequestedOrders() {
        Log.i("TAG","loading requestedOrders")
        val requestedOrders = userOrdersAdminViewModel.requestedOrders
        if (requestedOrders.isEmpty()){
            binding.userOrdersAdminViewRV.visibility = View.GONE
            binding.noItemsIndicator.text = "No Requested Orders For now..."
            return
        }
        binding.userOrdersAdminViewRV.visibility = View.VISIBLE
        userOrdersAdminViewAdapter?.setList(requestedOrders)

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
                //if the list is empty then we will hide the RV and show a text saying "No Accepted Orderssss"
                binding.idPBLoading.visibility = View.INVISIBLE
                if (it.isEmpty()){
                    binding.userOrdersAdminViewRV.visibility = View.GONE
                    binding.noItemsIndicator.text = "No Accepted Orders For now..."
                    Log.i(tag,"this is empty boss")
                    return@Observer
                }
                binding.noItemsIndicator.visibility = View.GONE
                binding.userOrdersAdminViewRV.visibility = View.VISIBLE
                userOrdersAdminViewAdapter!!.setList(it)
            }
        })

        userOrdersAdminViewModel.getRequestedOrders().observe(this, Observer {
            if (userOrdersAdminViewModel.getSpinnerPosition()== 1){
                //if the list is empty then we will hide the RV and show a text saying "No Requested Orderssss"
                binding.idPBLoading.visibility = View.INVISIBLE
                if (it.isEmpty()){
                    binding.noItemsIndicator.text = "No Requested Orders For now..."
                    binding.userOrdersAdminViewRV.visibility = View.GONE
                    return@Observer
                }
                binding.noItemsIndicator.visibility = View.GONE
                binding.userOrdersAdminViewRV.visibility = View.VISIBLE
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