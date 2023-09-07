package com.andrayudu.sureshdiaryfoods.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.ActivityDayProductionReportManagingBinding
import com.andrayudu.sureshdiaryfoods.model.ProductionReportModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DayProductionReportManaging : AppCompatActivity() {

    private lateinit var binding:ActivityDayProductionReportManagingBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var productionReportReference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_day_production_report_managing)
        mAuth = FirebaseAuth.getInstance()
        productionReportReference = FirebaseDatabase.getInstance().getReference("ProductionReference")
        //in this a child will be created with the current date and inside it we will create the todays production report


        binding.btnEnterData.setOnClickListener {
            postProductionReport()

        }
    }

    private fun postProductionReport() {
        val normalBoxes: String = binding.normalBoxes.getText().toString()
        val specialBoxes: String = binding.specialBoxes.getText().toString()
        val sugarBoxes: String = binding.sugarKovaBoxes.getText().toString()
        val sugarlessBoxes: String = binding.sugarlessBoxes.getText().toString()
        val kalakandBoxes: String = binding.geminiKalakandBoxes.getText().toString()


        val date  = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        val formattedTodaysDate = date.format(formatter)


       val postingProduction =  productionReportReference.child(formattedTodaysDate).setValue(ProductionReportModel(normalBoxes,specialBoxes,sugarBoxes,sugarlessBoxes,kalakandBoxes))

        if (postingProduction.isSuccessful){
            Toast.makeText(this,"Production Report Submitted Succesfilly",Toast.LENGTH_SHORT).show()
        }





    }
}