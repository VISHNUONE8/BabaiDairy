package com.andrayudu.sureshdiaryfoods

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.andrayudu.sureshdiaryfoods.adapters.DaySaleReportAdapter
import com.andrayudu.sureshdiaryfoods.databinding.ActivityStockBinding
import com.andrayudu.sureshdiaryfoods.model.DaySaleReportModel
import com.andrayudu.sureshdiaryfoods.model.ProductionReportModel
import com.google.firebase.database.*
import java.text.Format
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

//this activity displays the amount of stock that the dairy has after delivering yesterdays orders..
//i.e dayProduction - daySale  = Stock
//dayProduction is not only dayProduction (it may or may not contain yesterdays stock) ask anna for clarity on this....
class StockActivity : AppCompatActivity() {

    private lateinit var binding:ActivityStockBinding
    private lateinit var productionReference:DatabaseReference
    private lateinit var saleReference:DatabaseReference
    var productionNormalKova = 0
    var productionSpecialKova = 0
    var saleNormalKova = 0
    var saleSpecialKova = 0

    private var list:ArrayList<DaySaleReportModel>? = null
    var date = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_stock)


        binding.datePickerLayout.setEndIconOnClickListener {
            showCalendar()
        }
        productionReference  = FirebaseDatabase.getInstance().getReference("ProductionReference")
        saleReference  = FirebaseDatabase.getInstance().getReference("DaySaleReport")

        binding.btnStock.setOnClickListener {
            val dateOfStock = binding.datePickerET.text.toString()
            date = dateOfStock
            getProductionReport()


        }



    }

    private fun showCalendar() {
        // c is the instance of calendar
        val c = Calendar.getInstance()

        // on below line we are getting
        // our day, month and year.
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { view,year,monthOfYear,dayOfMonth ->
                // on below line we are setting
                // date to our text view.

                //here we add +1 to the monthOfYear because it is being returned one val less than the current month
                val monthSelected = getDoubleDigitDate((monthOfYear+1).toString())
                val daySelected = getDoubleDigitDate(day.toString())
                val editTextdate =  ("$daySelected-$monthSelected-$year")

                binding.datePickerET.setText(editTextdate)

            },
            // on below line we are passing year, month
            // and day for the todays so it will be selected by default date in our date picker.
            year,
            month,
            day

        )

        // at last we are calling show
        // to display our date picker dialog.
        datePickerDialog.show()

    }

    private fun getDoubleDigitDate(i: String):String {
        var returnVal = ""
        when(i){
            "1"->{
                returnVal= "01"
            } "2"->{
                returnVal= "02"
            } "3"->{
                returnVal= "03"
            } "4"->{
                returnVal= "04"
            } "5"->{
                returnVal= "05"
            } "6"->{
                returnVal= "06"
            } "7"->{
                returnVal= "07"
            } "8"->{
                returnVal= "08"
            } "9"->{
                returnVal= "09"
            }
            else->{
                returnVal =i
            }
        }

        return returnVal




    }

    private fun getDaySaleReport() {

        list = ArrayList()


        val todaySales = saleReference.child(date)
        todaySales.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    list!!.clear()
                    //above line ensures that the array list doesnt contain any duplicate values....
                    for (Datasnapshot in snapshot.children){
                        val daysaleReport: DaySaleReportModel? = Datasnapshot.getValue(
                            DaySaleReportModel::class.java)
                        saleNormalKova = saleNormalKova+(daysaleReport?.NormalBox?.toInt())!!
                        saleSpecialKova = saleSpecialKova+(daysaleReport?.SplBox?.toInt())!!
                        list!!.add(daysaleReport!!)

                    }

                    binding.stockTV.append("normal kova = ${productionNormalKova-saleNormalKova}  specialkova = ${productionSpecialKova-saleSpecialKova}")
                    binding.dateTV.append(date)
                    Log.i("TAG","The stock is: normal kova = ${productionNormalKova-saleNormalKova}  specialkova = ${productionSpecialKova-saleSpecialKova}")
                    Log.i("TAG","todays report from stock Activity is:"+list.toString())

                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })


    }


    private fun getProductionReport() {


        val todaysProduction = productionReference.child(date)
        todaysProduction.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val prod: ProductionReportModel? =
                        snapshot.getValue(ProductionReportModel::class.java)

                    productionNormalKova = prod?.NormalBoxes?.toInt()!!
                    productionSpecialKova = prod.SpecialBoxes?.toInt()!!

                    Log.i("TAG","normal prod count is:"+productionNormalKova.toString())


                }
                getDaySaleReport()
            }

            override fun onCancelled(error: DatabaseError) {
            }

        })

    }
}