package com.andrayudu.sureshdiaryfoods.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.adapters.DaySaleReportAdapter
import com.andrayudu.sureshdiaryfoods.databinding.ActivityDaySaleReportBinding
import com.andrayudu.sureshdiaryfoods.model.DaySaleReportModel
import com.google.firebase.database.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class DaySaleReport : AppCompatActivity() {

    private lateinit var binding:ActivityDaySaleReportBinding
    private lateinit var DaySaleReportReference:DatabaseReference
    private lateinit var recyclerView:RecyclerView
    private lateinit var myAdapter:DaySaleReportAdapter
    var date:String? = null

    private var nameSuggestions = arrayOf("ajay","bindu","chintu","dinesh","Naresh","Ramesh","LocalSale")

    private var daySales:MutableLiveData<List<DaySaleReportModel>?> = MutableLiveData()
    private var list:ArrayList<DaySaleReportModel>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_day_sale_report)


        val current = LocalDateTime.now()
        val dateformatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
         date = current.format(dateformatter)

        val customerNameSuggestionAdapter = ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,nameSuggestions)
        binding.customerNameAuto.setAdapter(customerNameSuggestionAdapter)
        binding.customerNameAuto.threshold = 2
        binding.customerNameAuto

        binding.customerNameAuto.setOnItemClickListener { parent, view, position, id ->
            //whatever might be the item clicked,then only we will display the day sale report posting layout

            binding.normalBoxETlayout.visibility = View.VISIBLE
            binding.billNoETlayout.visibility = View.VISIBLE
            binding.specialBoxETlayout.visibility = View.VISIBLE
            binding.billAmountETlayout.visibility = View.VISIBLE
            binding.btnPost.visibility = View.VISIBLE
        }

        DaySaleReportReference = FirebaseDatabase.getInstance().getReference("DaySaleReport").child(date!!)

        getDaySaleReport()

        binding.btnPost.setOnClickListener {
            postReport()
        }

        daySales.observe(this, Observer {
            myAdapter.setList(it)

            Log.i("TAG","the sales report is :"+it.toString())
        })

    }

    private fun postReport() {

        val customerName: String = binding.customerNameAuto.getText().toString()
        val billNo: String = binding.billNoET.getText().toString()
        val normalBox: String = binding.normalBoxET.getText().toString()
        val specialBox: String = binding.specialBoxET.getText().toString()
        val billAmount: String = binding.billAmountET.getText().toString()



        if (date!=null){
           val postReporttoFirebase =  DaySaleReportReference.child(customerName).setValue(DaySaleReportModel(customerName,billNo,normalBox,specialBox,billAmount))
            if (postReporttoFirebase.isSuccessful){
                Toast.makeText(this,"updated the production report successfully",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getDaySaleReport() {
        recyclerView = binding.recyclerView
        recyclerView.setHasFixedSize(true)

        recyclerView.layoutManager = LinearLayoutManager(this)

        list = ArrayList()
        myAdapter = DaySaleReportAdapter(list!!)
        recyclerView.adapter = myAdapter




        Log.i("TAG","todays date is:"+date)
        DaySaleReportReference.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()){
                    list!!.clear()
                    //above line ensures that the array list doesnt contain any duplicate values....
                    for (Datasnapshot in snapshot.children){
                        val daysaleReport:DaySaleReportModel? = Datasnapshot.getValue(DaySaleReportModel::class.java)
                        list!!.add(daysaleReport!!)

                    }
                    daySales.postValue(list)
                    myAdapter.setList(list)
                    Log.i("TAG","todays report is:"+list.toString())

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


    }
}