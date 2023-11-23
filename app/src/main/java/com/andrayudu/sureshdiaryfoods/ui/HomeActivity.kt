package com.andrayudu.sureshdiaryfoods.ui

import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.andrayudu.sureshdiaryfoods.HomeActivityViewModel
import com.andrayudu.sureshdiaryfoods.NetworkConnection
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

/*Done clearCoding*/
class HomeActivity : AppCompatActivity() {

    private val TAG = "HomeActivity"

    private val viewModel:HomeActivityViewModel by viewModels()

    private lateinit var networkConnection:NetworkConnection


    //UI components
    private lateinit var actionBarTextView: TextView
    private lateinit var binding: ActivityHomeBinding



    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        networkConnection = NetworkConnection(applicationContext)

        initViews()
        setNavController()
        initOnbackPressedDispatcher()
        initObservers()
        checkPermissions()
        viewModel.loadItemsCatalogue()
        viewModel.subscribeToSDF()


    }
    private fun initObservers() {
        networkConnection.observe(this) { isConnected ->
            if (isConnected) {
                //if the internet is connected and also the itemsCatalogue is loaded then we will display the fragments
                if (viewModel.isLoaded){
                    binding.navigationHostFragment.visibility = View.VISIBLE
                }
                binding.actionbar.visibility = View.VISIBLE
                binding.noInternetLayout.visibility = View.GONE
            } else {
                binding.navigationHostFragment.visibility = View.GONE
                binding.actionbar.visibility = View.GONE
                binding.noInternetLayout.visibility = View.VISIBLE
                Toast.makeText(this, "No Internet Connection", Toast.LENGTH_LONG).show()
            }
        }

        viewModel.itemsCatalogueLive.observe(this, Observer { itemsCatalogueModel->
            itemsCatalogueModel.let {
                binding.progressBarHome.visibility = View.GONE
                binding.navigationHostFragment.visibility = View.VISIBLE
            }
        })

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
        builder.setTitle("Exit !")
        builder.setMessage("Are you Sure You want to Exit?")
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


    //only valid from android 13 and above versions
    //10,11,12 versions are enabled with notifications by default
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissions() {

       val perms =  arrayOf(android.Manifest.permission.POST_NOTIFICATIONS)
       val permsRequestCode = 200
       requestPermissions(perms,permsRequestCode)
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

    private fun setNavController(){
        try{
            val navHostFragment = supportFragmentManager.findFragmentById(R.id.navigationHostFragment) as NavHostFragment
            val navController = navHostFragment.navController

            // Find reference to bottom navigation view
            val navView: BottomNavigationView = findViewById(R.id.bottomNavigation)
            // Hook your navigation controller to bottom navigation view
            navView.setupWithNavController(navController)
        } catch (e:Exception){
            Log.e(TAG,"there is an exception${e.message.toString()}")
        }
    }


}
