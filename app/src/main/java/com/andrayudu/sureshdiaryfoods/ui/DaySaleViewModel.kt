package com.andrayudu.sureshdiaryfoods.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrayudu.sureshdiaryfoods.model.DaySaleReportModel
import com.andrayudu.sureshdiaryfoods.model.UserRegisterModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DaySaleViewModel:ViewModel() {

    private val tag = "DaySaleViewModel"

    private var spinnerPosition:Int = 0
    val customerName = MutableLiveData<String>()

    val billNo = MutableLiveData<String>()
    val normalBox  = MutableLiveData<String>()
    val specialBox = MutableLiveData<String>()
    val billAmount = MutableLiveData<String>()
    val statusUpdater = MutableLiveData<String>()

    private val autoCompleteTVNamesLive = MutableLiveData<List<String>>()

    private val saleReport = MutableLiveData<List<DaySaleReportModel>?>()
    private val saleList =ArrayList<DaySaleReportModel>()
    private var autoCompleteTVNamesList =ArrayList<String>()


    private val  DaySaleReportReference = FirebaseDatabase.getInstance().getReference("DaySaleReport")


    fun getSpinnerNamesList():LiveData<List<String>> {
        return autoCompleteTVNamesLive
    }


    fun getStatusUpdater():LiveData<String> {
        return statusUpdater
    }

    fun setSpinnerPosition(position:Int){
        this.spinnerPosition = position
    }
    fun getSpinnerPosition():Int{
        return spinnerPosition
    }

    fun getSaleReport():LiveData<List<DaySaleReportModel>?>{
        return saleReport
    }
     fun postReport() {

        val customername: String? = customerName.value
        val billno: String? = billNo.value
        val normalbox: String? = normalBox.value
        val specialbox: String? = specialBox.value
        val billAmount: String? = billAmount.value

         Log.i("TAG","bill amount is "+billAmount)

         val date  = LocalDate.now()
         val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
         val formattedTodaysDate = date.format(formatter)
         val setDaySaleReference = DaySaleReportReference.child(formattedTodaysDate)

         val daySaleReportModel = DaySaleReportModel(customername,billno,normalbox,specialbox,billAmount)
        if (date!=null){
            viewModelScope.launch {
                val postingTask =  setDaySaleReference.child(customername!!).setValue(daySaleReportModel)
                postingTask.await()
                if (postingTask.isSuccessful){
                    statusUpdater.postValue("1")
                }
                else{
                    statusUpdater.postValue("-1")

                }
            }

        }
    }


    fun loadSpinnerNames(){
        viewModelScope.launch(Dispatchers.IO) {
            val userReference = FirebaseDatabase.getInstance().getReference("Users")
            userReference.addValueEventListener(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()){
                        autoCompleteTVNamesList.clear()
                        for (Datasnapshot in snapshot.children){
                            val user:UserRegisterModel? = Datasnapshot.getValue(UserRegisterModel::class.java)
                            autoCompleteTVNamesList.add(user?.Name!!)
                        }
                        Log.i(tag,"Spinner Names are"+autoCompleteTVNamesList.toString())
                        autoCompleteTVNamesLive.postValue(autoCompleteTVNamesList)

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        }
    }
    fun loadSaleReport(date:String) {
        viewModelScope.launch(Dispatchers.IO) {

            val todayReference = DaySaleReportReference.child(date)
            todayReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()){
                        saleList.clear()
                        //above line ensures that the array list doesnt contain any duplicate values....
                        for (Datasnapshot in snapshot.children){
                            val daysaleReport:DaySaleReportModel? = Datasnapshot.getValue(DaySaleReportModel::class.java)
                            saleList.add(daysaleReport!!)
                        }
                        saleReport.postValue(saleList)
                    }
                    else{
                        saleReport.postValue(null)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                }
            })
        }
    }
}