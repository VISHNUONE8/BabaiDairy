package com.andrayudu.sureshdiaryfoods.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.ActivityStockReportBinding
import com.google.android.material.snackbar.Snackbar
import java.util.*

//this activity displays the amount of stock that the dairy has after delivering yesterdays orders..
//i.e dayProduction - daySale  = Stock
//dayProduction is not only dayProduction (it may or may not contain yesterdays stock) ask anna for clarity on this....
class StockReportActivity : AppCompatActivity() {

    private lateinit var binding:ActivityStockReportBinding
    private lateinit var stockReportViewModel: StockReportViewModel
    private lateinit var actionBarBackButton: ImageView
    private lateinit var actionBarTextView: TextView




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_stock_report)
        stockReportViewModel = ViewModelProvider(this)[StockReportViewModel::class.java]

        actionBarBackButton = binding.actionbarStockReport.findViewById(R.id.actionbar_Back)
        actionBarTextView = binding.actionbarStockReport.findViewById(R.id.actionbar_Text)
        actionBarTextView.text = "Stock Report"


        actionBarBackButton.setOnClickListener {
            onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
                override fun handleOnBackPressed() {

                    finish()
                }
            })
            onBackPressedDispatcher.onBackPressed()
        }


        initObservers()
        setClickListeners()


    }

    private fun setClickListeners() {
        binding.datePickerLayout.setEndIconOnClickListener {
            showCalendar()
        }
    }

    private fun initObservers() {
        stockReportViewModel.getStockLiveData().observe(this, Observer {
            if (it == "-1"){

                binding.stockDisplayLayout.visibility = View.GONE
                snackbar("Stock Report is not available on this day")

            }
            else if(it !=null){
                binding.stockDisplayLayout.visibility = View.VISIBLE
                binding.stockDisplayTV.text = it
            }
        })
    }
    fun snackbar(msg:String){
        Snackbar.make(binding.layout,msg, Snackbar.LENGTH_LONG).show()
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
                val daySelected = getDoubleDigitDate(dayOfMonth.toString())
                val editTextdate =  ("$daySelected-$monthSelected-$year")

                binding.datePickerET.setText(editTextdate)
                binding.datePickerET.clearFocus()
                //first the production report will be loaded and then the salereport will be loaded and finally (production-sale) will be done......
                stockReportViewModel.loadProductionReport(editTextdate)


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



}