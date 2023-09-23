package com.andrayudu.sureshdiaryfoods.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.DaySaleReportItemBinding
import com.andrayudu.sureshdiaryfoods.databinding.LayoutCartItemBinding
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.model.DaySaleReportModel

class DaySaleReportAdapter():RecyclerView.Adapter<SaleReportViewHolder>() {
    private var daySaleList = ArrayList<DaySaleReportModel>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SaleReportViewHolder {

        val layoutInflater
                = LayoutInflater.from(parent.context)
        val binding : DaySaleReportItemBinding =
            DataBindingUtil.inflate(layoutInflater, R.layout.day_sale_report_item,parent,false)
        return SaleReportViewHolder(binding)

    }

    override fun getItemCount(): Int {
       return daySaleList.size

    }

    override fun onBindViewHolder(holder: SaleReportViewHolder, position: Int) {
        holder.bind(daySaleList[position])

    }
    fun setList(saleReportFirebase:List<DaySaleReportModel>?){
        daySaleList.clear()
        daySaleList.addAll(saleReportFirebase!!)
        notifyDataSetChanged()
    }


}


class SaleReportViewHolder(val binding: DaySaleReportItemBinding):RecyclerView.ViewHolder(binding.root){
    fun bind(
        daySaleReportModel: DaySaleReportModel){


       binding.customerName.text = daySaleReportModel.CustomerName
       binding.billNoTV.text = daySaleReportModel.BillNo
       binding.billAmountTV.text = daySaleReportModel.BillAmount
        binding.splBoxTV.text = daySaleReportModel.SplBox
       binding.normalBoxTV.text = daySaleReportModel.NormalBox


//        binding.itemHolder.setOnClickListener{
//            clickListener(specializationType)
//        }




    }
}