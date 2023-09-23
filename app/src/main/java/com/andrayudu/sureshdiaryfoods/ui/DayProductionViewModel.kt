package com.andrayudu.sureshdiaryfoods.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrayudu.sureshdiaryfoods.model.ProductionReportModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DayProductionViewModel():ViewModel() {

    val TAG = "DayProductionViewModel"



    private  val mAuth = FirebaseAuth.getInstance()
    private  val productionReportReference = FirebaseDatabase.getInstance().getReference("ProductionReference")
    private var spinnerPosition = 0

    val normalBoxes = MutableLiveData<String>()
    val specialBoxes = MutableLiveData<String>()
    val sugarlesBoxes = MutableLiveData<String>()
    val sugarkovaBoxes = MutableLiveData<String>()
    val geminiKalakandBoxes = MutableLiveData<String>()
    val statusUpdater = MutableLiveData<String>()
    val productionReport = MutableLiveData<ProductionReportModel?>()




    fun getProductionReport():LiveData<ProductionReportModel?>{
        return productionReport
    }

    fun getStatusUpdater():LiveData<String>{
        return statusUpdater
    }

    fun getSpinnerPosition():Int{
        return spinnerPosition
    }
    fun setSpinnerPosition(spinnerPosition:Int){
        this.spinnerPosition = spinnerPosition
    }

    fun loadProductionReport(editTextdate: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val gettingTask = FirebaseDatabase.getInstance().getReference("ProductionReference")
                .child(editTextdate).get()
            gettingTask.addOnSuccessListener {
                val productionReportModel = it.getValue(ProductionReportModel::class.java)
                productionReport.postValue(productionReportModel)
            }
        }
    }

     fun postProductionReport() {
        val normalBoxesCount : String? = normalBoxes.value
        val specialBoxesCount: String? = specialBoxes.value
        val sugarBoxesCount: String? = sugarkovaBoxes.value
        val sugarlessBoxesCount: String? = sugarlesBoxes.value
        val kalakandBoxesCount: String? = geminiKalakandBoxes.value

        val date  = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val formattedTodaysDate = date.format(formatter)

        val productionModel =ProductionReportModel(normalBoxesCount,specialBoxesCount,sugarBoxesCount,sugarlessBoxesCount,kalakandBoxesCount)

        viewModelScope.launch (Dispatchers.IO){
            val postingProduction =  productionReportReference.child(formattedTodaysDate).setValue(productionModel)
            postingProduction.await()
            if (postingProduction.isSuccessful){
                statusUpdater.postValue("1")
            }
            else{
                statusUpdater.postValue("-1")
            }
    }
    }

}