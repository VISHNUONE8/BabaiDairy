package com.andrayudu.sureshdiaryfoods.ui

import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.andrayudu.sureshdiaryfoods.HomeActivityViewModel
import com.andrayudu.sureshdiaryfoods.NetworkConnection
import com.andrayudu.sureshdiaryfoods.fragments.HomeFragment
import com.andrayudu.sureshdiaryfoods.fragments.OrdersFragment
import com.andrayudu.sureshdiaryfoods.fragments.ProfileFragment
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity(),BottomNavigationView.OnNavigationItemSelectedListener {

    private val tag = "HomeActivity"

    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController
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
        initObservers()

        //checking whether the user is an admin or not
        //so that we can give a subscription to notif channel...
        viewModel.userOrAdmin()
        checkPermissions()


        binding.bottomNavigation.selectedItemId = R.id.Home
        binding.bottomNavigation.setOnItemSelectedListener (this)

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
    private fun setNavController() {

        //for using navigation graph
        try {
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.navigationHostFragment) as NavHostFragment
            navController = navHostFragment.navController
            binding.bottomNavigation.setupWithNavController(navController)

        } catch (e: Exception) {
            e.message?.let {

            }
        }

    }

}
