package com.andrayudu.sureshdiaryfoods.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrayudu.sureshdiaryfoods.ui.DaySaleReport
import com.andrayudu.sureshdiaryfoods.ui.OrderDetails
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.ui.StockActivity
import com.andrayudu.sureshdiaryfoods.adapters.OrdersAdapter
import com.andrayudu.sureshdiaryfoods.databinding.FragmentOrdersBinding
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.model.OrderModel
import com.andrayudu.sureshdiaryfoods.ui.DayProductionReportManaging
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OrdersFragment : Fragment() {


     lateinit var binding:FragmentOrdersBinding
     var userId :String = "null"
     lateinit var mAuth: FirebaseAuth
    lateinit var mContext: Context
    private lateinit var adapter: OrdersAdapter
    lateinit var ordersLiveData:LiveData<List<CartItem>>
    lateinit var CustomerOrdersLiveData:LiveData<List<CartItem>>



    var ordersList = ArrayList<CartItem>()
    var datesList = ArrayList<String>()
     var ordersListSummary = ArrayList<String>()
    var customerOrdersList =ArrayList<OrderModel>()



    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_orders, container, false)
        mAuth = FirebaseAuth.getInstance()
        userId = mAuth.uid.toString()

        isAdmin(userId)





        return binding.root
    }
    private fun initRecyclerView() {
        binding.ordersRV.layoutManager = LinearLayoutManager(mContext)
        adapter = OrdersAdapter ({ selectedItem: OrderModel? -> listItemClicked(selectedItem!!) })
        binding.ordersRV.adapter = adapter
    }


   private fun listItemClicked(orderModel: OrderModel){
    Toast.makeText(mContext,"Selected food is ${orderModel.cartItemList}", Toast.LENGTH_SHORT).show()
       val intent = Intent(mContext, OrderDetails::class.java)
       intent.putExtra("orderModel",orderModel)
       startActivity(intent)
}


    fun getOrdersData(userId:String){

        datesList.clear()
        ordersList.clear()

           var ordersName = ""
           var Price = 0
           val ordersRef =  FirebaseDatabase.getInstance().getReference("Orders").child(userId)
           val customersordersRef =  FirebaseDatabase.getInstance().getReference("CustomerOrders").child(userId)
        //getting customer orders to the customerorders list

            customersordersRef.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        for (datasnapshot in snapshot.children){
                            val order = datasnapshot.getValue(OrderModel::class.java)
                            customerOrdersList.add(order!!)


                        }
                        Log.i("TAG","the order list is:"+customerOrdersList.toString())
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
           ordersRef.addValueEventListener(object : ValueEventListener{
               override fun onDataChange(snapshot: DataSnapshot) {
                   if (snapshot.exists()){
                       for (dataSnapshot in snapshot.children){
                           //this datasnapshot currespondss to the date and time heading
                            var prevDate = ""
                            for (subSnap in dataSnapshot.children){
                                val cartItem = subSnap.getValue(CartItem::class.java)
                                ordersList.add(cartItem!!)
                                if (prevDate == cartItem.Date){
                                    datesList.add("")
                                }
                                else{
                                    datesList.add(cartItem.Date)
                                    prevDate = cartItem.Date

                                }
                                ordersName = ordersName+cartItem.Name+"(${cartItem.Quantity}), "
                                Price = Price + ((cartItem.Price.toInt()) * (cartItem.Quantity.toInt()))


                            }
                            Log.i("TAG","Dates list is :"+datesList.toString())
                           ordersListSummary.add(ordersName)
                           adapter.setList(ordersList,datesList,customerOrdersList)
                           adapter.notifyDataSetChanged()
                           Log.i("the price is :",""+Price)


                       }
                       Log.i("MY TAG","the orders are:"+ordersList.toString())
                       Log.i("MY TAG","the orders names are:"+ordersListSummary.toString())
                   }
               }

               override fun onCancelled(error: DatabaseError) {
               }

           })





       }

    private fun isAdmin(userid:String?) {
        //if the user is an admin
        if (userid.equals("LcYIRtG0z4PuSI5tCdgRMUxaBjG3")){

            binding.adminPanelLayout.visibility = View.VISIBLE
            binding.ordersFragmentTV.text = "Admin Panel"

            //setting the onclickListeners
            binding.relLayoutProductionReport.setOnClickListener {
                startActivity(Intent(mContext, DayProductionReportManaging::class.java))
            }
            binding.relLayoutSaleReport.setOnClickListener {
                startActivity(Intent(mContext, DaySaleReport::class.java))
            }
            binding.relLayoutStockReport.setOnClickListener {
                startActivity(Intent(mContext, StockActivity::class.java))

            }

        }
        //if the user is not an admin,we will get the ordersdata and we will initialize recyclerview...
        else{
            getOrdersData(userId)
            initRecyclerView()

        }
    }




}