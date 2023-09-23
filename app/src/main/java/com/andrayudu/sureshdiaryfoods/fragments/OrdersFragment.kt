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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.adapters.OrdersAdapter
import com.andrayudu.sureshdiaryfoods.databinding.FragmentOrdersBinding
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.model.OrderModel
import com.andrayudu.sureshdiaryfoods.ui.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class OrdersFragment : Fragment() {


    private lateinit var binding:FragmentOrdersBinding
    private lateinit var ordersFragViewModel: OrdersFragViewModel
    private lateinit var mContext: Context
    private lateinit var adapter: OrdersAdapter

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
        ordersFragViewModel = ViewModelProvider(this)[OrdersFragViewModel::class.java]

        initObservers()
        ordersFragViewModel.isAdmin()

        return binding.root
    }

    private fun initObservers() {
        ordersFragViewModel.getStatus().observe(viewLifecycleOwner, Observer {


             if(it.equals("Customer")){
                 //if the user is not an admin,we will get the ordersdata and we will initialize recyclerview...
                 initRecyclerView()
                 ordersFragViewModel.getOrdersData()
            }
            else if (it.equals("Admin")){
                 binding.idPBLoading.visibility = View.GONE
                 binding.adminPanelLayout.visibility = View.VISIBLE
                 binding.ordersFragmentTV.text = "Admin Panel"

                 //setting the onclickListeners

                 binding.relLayoutUserOrders.setOnClickListener {
                     startActivity(Intent(mContext, UserOrdersAdminView::class.java))

                 }
                 binding.relLayoutProductionReport.setOnClickListener {
                     startActivity(Intent(mContext, DayProductionReport::class.java))
                 }
                 binding.relLayoutSaleReport.setOnClickListener {
                     startActivity(Intent(mContext, DaySaleReport::class.java))
                 }
                 binding.relLayoutStockReport.setOnClickListener {
                     startActivity(Intent(mContext, StockReportActivity::class.java))
                 }
            }


        })

        ordersFragViewModel.getOrdersListLive().observe(viewLifecycleOwner, Observer {
            binding.idPBLoading.visibility = View.GONE
            if (it.isEmpty()){
                binding.ordersRV.visibility = View.GONE
                binding.noOrdersIndicator.visibility = View.VISIBLE
                return@Observer
            }
            binding.noOrdersIndicator.visibility = View.GONE
            binding.ordersRV.visibility = View.VISIBLE
            adapter.setList(it,ordersFragViewModel.getDatesList())
        })

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




}