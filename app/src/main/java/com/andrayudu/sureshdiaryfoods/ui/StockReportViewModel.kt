package com.andrayudu.sureshdiaryfoods.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrayudu.sureshdiaryfoods.model.DaySaleReportModel
import com.andrayudu.sureshdiaryfoods.model.ProductionReportModel
import com.google.firebase.database.*
import kotlinx.coroutines.launch

class StockReportViewModel:ViewModel() {

    private  var productionReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("ProductionReference")
    private  var saleReference:DatabaseReference =  FirebaseDatabase.getInstance().getReference("DaySaleReport")

    private var productionNormalKova = 0
    private var productionSpecialKova = 0
    private var saleNormalKova = 0
    private var saleSpecialKova = 0

    private val stockLiveData = MutableLiveData<String>()
    private val saleReportList = ArrayList<DaySaleReportModel>()

    fun getStockLiveData():LiveData<String>{
        return stockLiveData
    }

    fun loadProductionReport(date: String) {
        viewModelScope.launch {

        val selectedDateProduction = productionReference.child(date)

        selectedDateProduction.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {

             //clearing the values before the calculation
             productionNormalKova = 0
             productionSpecialKova = 0
             saleNormalKova = 0
             saleSpecialKova = 0

         if (snapshot.exists()) {
             val prod: ProductionReportModel? =
                 snapshot.getValue(ProductionReportModel::class.java)

             productionNormalKova = prod?.NormalBoxes?.toInt()!!
             productionSpecialKova = prod.SpecialBoxes?.toInt()!!
         }
         //if the productionReport doesnt exist on that day
         //we will display a snackbar "stock report not available on this day"
         else{
                stockLiveData.postValue("-1")
                return
         }
         //after prod snap is fetched , we will get day sale report
         getDaySaleReport(date)
        }

        override fun onCancelled(error: DatabaseError) {
        }
        })
        }
    }

    private fun getDaySaleReport(date:String) {

        val todaySales = saleReference.child(date)
        todaySales.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    saleReportList.clear()
                    //above line ensures that the array list doesnt contain any duplicate values....
                    for (Datasnapshot in snapshot.children){
                        val daysaleReport: DaySaleReportModel? = Datasnapshot.getValue(
                            DaySaleReportModel::class.java)
                        saleNormalKova = saleNormalKova+(daysaleReport?.NormalBox?.toInt())!!
                        saleSpecialKova = saleSpecialKova+(daysaleReport.SplBox?.toInt())!!
                        saleReportList.add(daysaleReport)
                    }

                    stockLiveData.postValue("Normalkova = ${productionNormalKova-saleNormalKova}\nSpecialkova = ${productionSpecialKova-saleSpecialKova}")

                }
                //if the saleReport doesnt exist on that day
                //we will display a snackbar "stock report not available on this day"
                else{
                    stockLiveData.postValue("-1")
                    return
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })


    }



}