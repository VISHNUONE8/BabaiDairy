package com.andrayudu.babaidairy.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrayudu.babaidairy.ui.HomeActivityViewModel
import com.andrayudu.babaidairy.R
import com.andrayudu.babaidairy.adapters.OrdersRVAdapter
import com.andrayudu.babaidairy.databinding.FragmentOrdersBinding
import com.andrayudu.babaidairy.model.OrderModel
import com.andrayudu.babaidairy.ui.*

class OrdersFragment : Fragment() {

    private val TAG = "OrdersFragment"

    private val sharedViewModel : HomeActivityViewModel by activityViewModels()

    //UI Components
    private lateinit var binding:FragmentOrdersBinding
    private lateinit var mContext: Context
    private lateinit var adapter: OrdersRVAdapter




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

        initObservers()
        initRecyclerView()
        sharedViewModel.callLoadOrdersData()



        return binding.root
    }


    private fun initObservers() {
        sharedViewModel.ordersListLive.observe(viewLifecycleOwner) {
            binding.idPBLoading.visibility = View.GONE
            //if the customers has no previous orders then No orders Yet will be displayed..
            if (it.isEmpty()){
                binding.ordersRV.visibility = View.GONE
                binding.noOrdersIndicator.visibility = View.VISIBLE
                return@observe
            }
            binding.noOrdersIndicator.visibility = View.GONE
            binding.ordersRV.visibility = View.VISIBLE
            adapter.setList(it,sharedViewModel.datesList)
        }
    }


    private fun initRecyclerView() {
        binding.ordersRV.layoutManager = LinearLayoutManager(mContext)
        adapter = OrdersRVAdapter { selectedItem: OrderModel? -> listItemClicked(selectedItem) }
        binding.ordersRV.adapter = adapter
    }


   private fun listItemClicked(orderModel: OrderModel?){
       val intent = Intent(mContext, OrderDetailsActivity::class.java)
       intent.putExtra("orderModel",orderModel)
       startActivity(intent)
}
}