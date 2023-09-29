package com.andrayudu.sureshdiaryfoods.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.adapters.DaySaleReportAdapter
import com.andrayudu.sureshdiaryfoods.databinding.ActivityDaySaleReportBinding
import com.andrayudu.sureshdiaryfoods.model.DaySaleReportModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseReference
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


class DaySaleReport : AppCompatActivity() {

    private lateinit var binding:ActivityDaySaleReportBinding
    private lateinit var daySaleViewModel: DaySaleViewModel
    private lateinit var recyclerView:RecyclerView
    private lateinit var myAdapter:DaySaleReportAdapter
    private lateinit var actionBarBackButton: ImageView
    private lateinit var actionBarTextView: TextView

//    private var nameSuggestions = arrayOf("ajay","bindu","chintu","dinesh","Naresh","Ramesh","LocalSale")
    private val spinnerSuggestions = arrayOf("GetDaySaleReport","SetDaySaleReport")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_day_sale_report)
        daySaleViewModel = ViewModelProvider(this)[DaySaleViewModel::class.java]
        binding.lifecycleOwner = this
        binding.daySaleViewModel = daySaleViewModel


        actionBarBackButton = binding.actionbarDaySale.findViewById(R.id.actionbar_Back)
        actionBarTextView = binding.actionbarDaySale.findViewById(R.id.actionbar_Text)
        actionBarTextView.text = "DaySale Report"



        actionBarBackButton.setOnClickListener {
            onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
                override fun handleOnBackPressed() {

                    finish()
                }
            })
            onBackPressedDispatcher.onBackPressed()
        }






        daySaleViewModel.loadSpinnerNames()
        initRecyclerView()
        initSpinner()
        initObservers()

        binding.datePickerLayout.setEndIconOnClickListener {
            showCalendar()
        }

        binding.customerNameAuto.setOnItemClickListener { parent, view, position, id ->
            //whatever might be the item clicked,then only we will display the day sale report posting layout

            binding.normalBoxETlayout.visibility = View.VISIBLE
            binding.billNoETlayout.visibility = View.VISIBLE
            binding.specialBoxETlayout.visibility = View.VISIBLE
            binding.billAmountETlayout.visibility = View.VISIBLE
            binding.btnPost.visibility = View.VISIBLE
        }

        binding.btnPost.setOnClickListener {
            if(validateEntries()){
                hideKeyboard(this)
                daySaleViewModel.postReport()
            }
        }
    }

    private fun initAutoCompleteTV(nameSuggestions:List<String>) {
        val customerNameSuggestionAdapter = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,nameSuggestions)
        binding.customerNameAuto.setAdapter(customerNameSuggestionAdapter)
        binding.customerNameAuto.threshold = 2
        binding.customerNameAuto
    }

    private fun initRecyclerView() {
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        myAdapter = DaySaleReportAdapter()
        recyclerView.adapter = myAdapter
    }

    private fun initObservers() {
        daySaleViewModel.getSaleReport().observe(this, Observer {
            if (it!=null){
                myAdapter.setList(it)
            }
            else{
                snackbar("Sale Report not available on this day")
            }
        })

        //this observes the setting status
        daySaleViewModel.getStatusUpdater().observe(this, Observer {
            if(it == "1"){
                snackbar("Day Sale Report Posted Successfully")
                clearEditTexts()
            }
            else{
                snackbar("Operation Unsuccessful Please try after sometime")
            }

        })

        daySaleViewModel.getSpinnerNamesList().observe(this, Observer {
            if (it!=null){

                initAutoCompleteTV(it)
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
        binding.billAmountET.clearFocus()
//        it will not be cleared because to note whose value has been updated last
//        binding.customerNameAuto.text?.clear()
        binding.billNoET.text?.clear()
        binding.normalBoxET.text?.clear()
        binding.specialBoxET.text?.clear()
        binding.billAmountET.text?.clear()
    }

    fun snackbar(msg:String){
        Snackbar.make(binding.layout,msg, Snackbar.LENGTH_LONG).show()
    }

    fun validateEntries():Boolean{
        if (binding.customerNameAuto.text.toString().isEmpty()){
            binding.customerNameAuto.setError("This cannot be empty")
            binding.customerNameAuto.requestFocus()
            return false

        }
        if (binding.normalBoxET.text.toString().isEmpty()){
            binding.normalBoxET.setError("This cannot be empty")
            binding.normalBoxET.requestFocus()
            return false
        }
        if (binding.specialBoxET.text.toString().isEmpty()){
            binding.specialBoxET.setError("This cannot be empty")
            binding.specialBoxET.requestFocus()
            return false
        }
        if (binding.billNoET.text.toString().isEmpty()){
            binding.billNoET.setError("This cannot be empty")
            binding.billNoET.requestFocus()
            return false
        }
        if (binding.billAmountET.text.toString().isEmpty()){
            binding.billAmountET.setError("This cannot be empty")
            binding.billAmountET.requestFocus()
            return false
        }
        return true
    }

    private fun initSpinner() {
        val spinnerAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,spinnerSuggestions)
        binding.saleReportSpinner.adapter =spinnerAdapter
        binding.saleReportSpinner.onItemSelectedListener = spinListener
        binding.saleReportSpinner.dropDownVerticalOffset = 100
    }
    private val spinListener: AdapterView.OnItemSelectedListener = object : AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            if (position == 0){
                daySaleViewModel.setSpinnerPosition( position)
                getSaleReportLayout()
            }
            else{
                daySaleViewModel.setSpinnerPosition( position)
                setSaleReportLayout()
            }
        }

        private fun setSaleReportLayout() {
            binding.getDaySaleReportLayout.visibility =View.GONE
            binding.setSaleReportLayout.visibility = View.VISIBLE
        }

        private fun getSaleReportLayout() {
            binding.setSaleReportLayout.visibility = View.GONE
            binding.getDaySaleReportLayout.visibility =View.VISIBLE
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
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
            { view,selectedyear,monthOfYear,dayOfMonth ->
                //we are inside the listener that will be called on selecting a date

                //here we add +1 to the monthOfYear because it is being returned one val less than the current month
                val monthSelected = getDoubleDigitDate((monthOfYear+1).toString())
                val daySelected = getDoubleDigitDate(dayOfMonth.toString())
                val editTextdate =  ("$daySelected-$monthSelected-$selectedyear")

                binding.datePickerET.setText(editTextdate)
                binding.datePickerET.clearFocus()
                daySaleViewModel.loadSaleReport(editTextdate)

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