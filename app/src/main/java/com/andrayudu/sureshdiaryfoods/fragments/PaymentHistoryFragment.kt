package com.andrayudu.sureshdiaryfoods.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrayudu.sureshdiaryfoods.ui.HomeActivityViewModel
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.adapters.PaymentHistoryRVAdapter
import com.andrayudu.sureshdiaryfoods.databinding.FragmentPaymentHistoryBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/*Done clearCoding*/
class PaymentHistoryFragment : Fragment() {

    private val TAG = "PaymentHistoryFragment"

    private  val sharedViewModel: HomeActivityViewModel by activityViewModels()


    //UI components
    private var adapter: PaymentHistoryRVAdapter? = null
    private lateinit var binding: FragmentPaymentHistoryBinding
    private lateinit var mContext: Context


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_payment_history, container, false)




        initRecyclerView()
        initObservers()
        initClickListeners()

        sharedViewModel.callLoadCustomerPayments()

        return binding.root
    }

    private fun initObservers() {

        sharedViewModel.paymentListLive.observe(viewLifecycleOwner, Observer { paymentsList ->

            //the below code snippets should be here only because in future wemay introduce different sorting techniques..
            //sorting payments list according to the date..
            val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
            val sortedList = paymentsList.sortedByDescending {
                LocalDate.parse(it.date, dateTimeFormatter)
            }
            adapter?.setList(sortedList)
        })
    }

    private fun initClickListeners() {

    }

    private fun initRecyclerView() {
        binding.paymentsRV.layoutManager = LinearLayoutManager(mContext)
        adapter = PaymentHistoryRVAdapter()
        binding.paymentsRV.adapter = adapter
    }


}