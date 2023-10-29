package com.andrayudu.sureshdiaryfoods.ui

import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.andrayudu.sureshdiaryfoods.HomeActivityViewModel
import com.andrayudu.sureshdiaryfoods.NetworkConnection
import com.andrayudu.sureshdiaryfoods.fragments.HomeFragment
import com.andrayudu.sureshdiaryfoods.fragments.OrdersFragment
import com.andrayudu.sureshdiaryfoods.fragments.ProfileFragment
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.ActivityHomeBinding
import com.andrayudu.sureshdiaryfoods.fragments.PaymentHistoryFragment
import com.google.android.material.navigation.NavigationBarView

/*Done clearCoding*/
class HomeActivity : AppCompatActivity(),NavigationBarView.OnItemSelectedListener {

    private val tag = "HomeActivity"

    private lateinit var binding: ActivityHomeBinding
    private val viewModel:HomeActivityViewModel by viewModels()

    //UI components
    private lateinit var actionBarTextView: TextView


    private lateinit var networkConnection:NetworkConnection

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        networkConnection = NetworkConnection(applicationContext)

        initViews()
        initOnbackPressedDispatcher()

        initObservers()

        //below function fetches the users outstanding and subscribes him to the SDF Notif channel
        viewModel.userInit()
        checkPermissions()


        binding.bottomNavigation.selectedItemId = R.id.Home
        binding.bottomNavigation.setOnItemSelectedListener (this)

    }

    private fun initOnbackPressedDispatcher() {
        onBackPressedDispatcher.addCallback(this,object :OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                //on pressing back from home Activity we should display a dialog are you sure you want to exit ...
                showAlertDialog()
            }

        })
    }

    private fun showAlertDialog() {

        val builder = AlertDialog.Builder(this)
        builder.setMessage("Are you Sure You want to exit the Application?")
        builder.setTitle("Exit !")
        builder.setCancelable(false)

        builder.setPositiveButton("Yes",(DialogInterface.OnClickListener { dialog, which ->
            finishAffinity()
        }))
        builder.setNegativeButton("No",(DialogInterface.OnClickListener { dialog, which ->
            dialog.cancel()
        }))

        val alertDialog = builder.create()
        alertDialog.show()
    }



    private fun initViews() {
        val actionBarText = "SureshDairyFoods"
        actionBarTextView = findViewById(R.id.actionbar_Home_Text)
        actionBarTextView.text = actionBarText
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    //only valid from android 13 and above versions
    //10,11,12 versions are enabled with notifications by default
    private fun checkPermissions() {

       val perms =  arrayOf(android.Manifest.permission.POST_NOTIFICATIONS)
       val permsRequestCode = 200
       requestPermissions(perms,permsRequestCode)
    }



    private fun initObservers() {
        networkConnection.observe(this, Observer {isConnected->
            if (isConnected){
                binding.navigationHostFragment.visibility = View.VISIBLE
                binding.actionbar.visibility = View.VISIBLE
                binding.noInternetLayout.visibility= View.GONE
            }
            else{
                binding.navigationHostFragment.visibility = View.GONE
                binding.actionbar.visibility = View.GONE
                binding.noInternetLayout.visibility= View.VISIBLE
                Toast.makeText(this,"No Internet Connection",Toast.LENGTH_LONG).show()
            }
        })

    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 200){
            if( (grantResults.isNotEmpty()) && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Notification Permission Granted", Toast.LENGTH_SHORT).show()            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {


        when(item.itemId){
            R.id.Home ->{
                if (binding.bottomNavigation.selectedItemId != R.id.Home){
                    replaceFragment(HomeFragment())
                }
            }
            R.id.Orders ->{

                if (binding.bottomNavigation.selectedItemId != R.id.Orders){
                    replaceFragment(OrdersFragment())
                }

            }
            R.id.Payments ->{

                if (binding.bottomNavigation.selectedItemId != R.id.Payments){
                    replaceFragment(PaymentHistoryFragment())
                }

            }
            R.id.Profile ->{
                if (binding.bottomNavigation.selectedItemId != R.id.Profile){
                    replaceFragment(ProfileFragment())
                }
            }
            else->{
                return false
            }
        }
        return true

    }

    //replaces the fragment as a transaction...
    private fun replaceFragment(fragment:Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.navigationHostFragment,fragment)
        transaction.commit()

    }

}
