package com.andrayudu.sureshdiaryfoods.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.ActivityDayProductionReportBinding
import com.google.android.material.snackbar.Snackbar
import java.util.*


class DayProductionReport : AppCompatActivity() {

    val TAG = "DayProductionReport"

    private lateinit var binding:ActivityDayProductionReportBinding
    private lateinit var dayProductionViewModel: DayProductionViewModel
    private lateinit var actionBarBackButton: ImageView
    private lateinit var actionBarTextView: TextView
    private val productionSpinnerItems = arrayOf("GetProductionReport","SetProductionReport")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_day_production_report)
        dayProductionViewModel = ViewModelProvider(this)[DayProductionViewModel::class.java]
        binding.lifecycleOwner = this
        binding.dayProductionViewModel = dayProductionViewModel

        actionBarBackButton = binding.actionbarDayProd.findViewById(R.id.actionbar_Back)
        actionBarTextView = binding.actionbarDayProd.findViewById(R.id.actionbar_Text)
        actionBarTextView.text = "DayProduction"



        actionBarBackButton.setOnClickListener {
            onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
                override fun handleOnBackPressed() {

                    finish()
                }
            })
            onBackPressedDispatcher.onBackPressed()
        }



        initSpinner()
        initObservers()

        binding.datePickerLayout.setEndIconOnClickListener {
            showCalendar()
        }

        binding.btnEnterData.setOnClickListener {
               if(validateEntries()){
                   hideKeyboard(this)
                 dayProductionViewModel.postProductionReport()

               }

        }
    }

    private fun initObservers() {
        dayProductionViewModel.getProductionReport().observe(this, Observer {
            if (it!=null) {
                binding.normalBoxesTV.text = "Normal Boxes: ${it.NormalBoxes}"
                binding.speciallBoxesTV.text = "Special Boxes: ${it.SpecialBoxes}"
                binding.sugarlessBoxesTV.text = "Sugarless Boxes: ${it.SugarLessBoxes}"
                binding.sugarKovaBoxesTV.text = "SugarKova Boxes: ${it.SugarBoxes}"
                binding.kalakandBoxesTV.text = "Kalakand Boxes: ${it.KalakandBoxes}"
            }
            //if there is no prod report on that day
            else{
                binding.normalBoxesTV.text = "Normal Boxes:"
                binding.speciallBoxesTV.text = "Special Boxes:"
                binding.sugarlessBoxesTV.text = "Sugarless Boxes:"
                binding.sugarKovaBoxesTV.text = "SugarKova Boxes:"
                binding.kalakandBoxesTV.text = "Kalakand Boxes:"
                snackbar("No Production Report found on the selected date")
            }
        })

        dayProductionViewModel.getStatusUpdater().observe(this, Observer {
            if(it == "1"){
                snackbar("Day Production Report Posted Successfully")
                clearEditTexts()
            }
            else{
                snackbar("Operation Unsuccessful Please try after sometime")
            }

        })

    }

    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun clearEditTexts() {
        //the focus should also be taken away from kalakand box
        binding.geminiKalakandBoxes.clearFocus()
        binding.normalBoxes.text?.clear()
        binding.specialBoxes.text?.clear()
        binding.sugarlessBoxes.text?.clear()
        binding.sugarKovaBoxes.text?.clear()
        binding.geminiKalakandBoxes.text?.clear()
    }

    fun snackbar(msg:String){
        Snackbar.make(binding.layout,msg,Snackbar.LENGTH_LONG).show()
    }
    fun validateEntries():Boolean{
        if (binding.normalBoxes.text.toString().isEmpty()){
            binding.normalBoxes.setError("This cannot be empty")
            return false

        }
        if (binding.specialBoxes.text.toString().isEmpty()){
            binding.specialBoxes.setError("This cannot be empty")
            return false
        }
        if (binding.sugarKovaBoxes.text.toString().isEmpty()){
            binding.sugarKovaBoxes.setError("This cannot be empty")
            return false
        }
        if (binding.sugarlessBoxes.text.toString().isEmpty()){
            binding.sugarlessBoxes.setError("This cannot be empty")
            return false
        }
        if (binding.geminiKalakandBoxes.text.toString().isEmpty()){
            binding.geminiKalakandLayout.setError("This cannot be empty")
            return false
        }
        return true
    }

    private fun initSpinner() {
        val spinnerAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,productionSpinnerItems)
        binding.productionSpinner.adapter =spinnerAdapter
        binding.productionSpinner.onItemSelectedListener = spinListener
        binding.productionSpinner.dropDownVerticalOffset = 100
    }
    private val spinListener: AdapterView.OnItemSelectedListener = object : AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (position == 0){
                dayProductionViewModel.setSpinnerPosition( position)
                binding.spinnerStatusText.text = "Get Production Report"
                getProductionReportLayout()
            }
            else{
                dayProductionViewModel.setSpinnerPosition( position)
                binding.spinnerStatusText.text = "Set Production Report"
                setProductionReportLayout()
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }

    }

    private fun getProductionReportLayout() {
        binding.setProductionReportLayout.visibility = View.GONE
        binding.getProductionReportLayout.visibility = View.VISIBLE
    }
    private fun setProductionReportLayout() {
        binding.getProductionReportLayout.visibility = View.GONE
        binding.setProductionReportLayout.visibility = View.VISIBLE
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
            { view,selectedyear,monthOfYear,dayOfMonth ->
                //we are inside the listener that will be called on selecting a date

                //here we add +1 to the monthOfYear because it is being returned one val less than the current month
                val monthSelected = getDoubleDigitDate((monthOfYear+1).toString())
                val daySelected = getDoubleDigitDate(dayOfMonth.toString())
                val editTextdate =  ("$daySelected-$monthSelected-$selectedyear")

                binding.datePickerET.setText(editTextdate)
                binding.datePickerET.clearFocus()
                dayProductionViewModel.loadProductionReport(editTextdate)

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