package com.andrayudu.babaidairy.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andrayudu.babaidairy.R
import com.andrayudu.babaidairy.databinding.RvItemPaymentHistoryBinding
import com.andrayudu.babaidairy.model.PaymentModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PaymentHistoryRVAdapter(): RecyclerView.Adapter<PaymentsHolder>() {

    private val paymentsList = ArrayList<PaymentModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentsHolder {
        val layoutInflater
                = LayoutInflater.from(parent.context)
        val binding : RvItemPaymentHistoryBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.rv_item_payment_history,parent,false)
        return PaymentsHolder(binding)
    }

    override fun getItemCount(): Int {
        return paymentsList.size
    }

    override fun onBindViewHolder(holder: PaymentsHolder, position: Int) {
        holder.bind(paymentsList[position])
    }

    fun setList(paymentListLoaded:List<PaymentModel>){
        paymentsList.clear()
        paymentsList.addAll(paymentListLoaded)
        notifyDataSetChanged()

    }

}

class PaymentsHolder(val binding:RvItemPaymentHistoryBinding):RecyclerView.ViewHolder(binding.root){
    fun bind(paymentModel:PaymentModel?){

       if(paymentModel!=null){
           binding.amountTV.text = paymentModel.amount.toString()

           val timeStamp = paymentModel.date
           val simpleDateFormat = SimpleDateFormat("dd-MM-yy  ", Locale.getDefault())
           val dateString = paymentModel.date

           binding.dateTV.text =dateString
           binding.viaTV.text = paymentModel.via
       }

    }

}