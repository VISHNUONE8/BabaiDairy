package com.andrayudu.sureshdiaryfoods.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrayudu.sureshdiaryfoods.HomeActivityViewModel
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.adapters.OrdersAdapter
import com.andrayudu.sureshdiaryfoods.databinding.FragmentOrdersBinding
import com.andrayudu.sureshdiaryfoods.model.OrderModel
import com.andrayudu.sureshdiaryfoods.ui.*

class OrdersFragment : Fragment() {

    private val TAG = "OrdersFragment"

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
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_orders, container, false)
        ordersFragViewModel = ViewModelProvider(this)[OrdersFragViewModel::class.java]

        initObservers()

        ordersFragViewModel.userOrAdmin()
        return binding.root
    }

    private fun initCustomersUI() {
        Log.i(TAG, "customer ui is loaded")
        initRecyclerView()
        ordersFragViewModel.loadOrdersData()
    }

    private fun initAdminUI() {
        Log.i(TAG, "admin ui is loaded")
        binding.adminPanelLayout.visibility = View.VISIBLE
        binding.ordersFragmentTV.text = getString(R.string.adminPanel)
        //setting the onclickListeners as only admin has access to them...
        initClickListeners()
    }

    private fun initObservers() {
        ordersFragViewModel.getOrdersListLive().observe(viewLifecycleOwner) {
            binding.idPBLoading.visibility = View.GONE
            if (it.isEmpty()){
                binding.ordersRV.visibility = View.GONE
                binding.noOrdersIndicator.visibility = View.VISIBLE
                return@observe
            }
            binding.noOrdersIndicator.visibility = View.GONE
            binding.ordersRV.visibility = View.VISIBLE
            adapter.setList(it,ordersFragViewModel.getDatesList())
        }

        ordersFragViewModel.getUserRoleLive().observe(viewLifecycleOwner, Observer {
            binding.idPBLoading.visibility = View.GONE
            if(it.equals("Admin")){
                Log.i(TAG,"admin ui is loaded using livedata")
               initAdminUI()
            }
            else{
               initCustomersUI()
            }
        })


    }

    private fun initClickListeners() {
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
        binding.relLayoutNotifications.setOnClickListener {
            startActivity(Intent(mContext, NotificationsActivity::class.java))
        }
        binding.RegisterUser.setOnClickListener {
            startActivity(Intent(mContext, RegisterActivity::class.java))
        }
    }

    private fun initRecyclerView() {
        binding.ordersRV.layoutManager = LinearLayoutManager(mContext)
        adapter = OrdersAdapter { selectedItem: OrderModel? -> listItemClicked(selectedItem) }
        binding.ordersRV.adapter = adapter
    }


   private fun listItemClicked(orderModel: OrderModel?){
       val intent = Intent(mContext, OrderDetails::class.java)
       intent.putExtra("orderModel",orderModel)
       startActivity(intent)
}
}