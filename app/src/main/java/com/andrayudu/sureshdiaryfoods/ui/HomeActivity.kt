package com.andrayudu.sureshdiaryfoods.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.andrayudu.sureshdiaryfoods.fragments.HomeFragment
import com.andrayudu.sureshdiaryfoods.fragments.OrdersFragment
import com.andrayudu.sureshdiaryfoods.fragments.ProfileFragment
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity(),BottomNavigationView.OnNavigationItemSelectedListener {


    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController

    private lateinit var actionBarTextView: TextView



    val homeFragment = HomeFragment()
    val ordersFragment = OrdersFragment()
    val profileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)

        actionBarTextView = findViewById(R.id.actionbar_Home_Text)

//
        binding.bottomNavigation.selectedItemId = R.id.Home
        binding.bottomNavigation.setOnItemSelectedListener (this)
        supportFragmentManager.beginTransaction().replace(
            R.id.navigationHostFragment
        , HomeFragment()
        )


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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.Home ->{
                supportFragmentManager.beginTransaction().replace(R.id.navigationHostFragment,homeFragment).commit()

                return true
            }
            R.id.Orders ->{
                supportFragmentManager.beginTransaction().replace(R.id.navigationHostFragment,ordersFragment).commit()

                return true
            }
            R.id.Profile ->{
                supportFragmentManager.beginTransaction().replace(R.id.navigationHostFragment,profileFragment).commit()

                return true
            }
            else->{
                return false
            }


        }

    }


//    private val navListener =
//        BottomNavigationView.OnNavigationItemSelectedListener { item: MenuItem ->
//            // By using switch we can easily get
//            // the selected fragment
//            // by using there id.
//            val itemId = item.itemId
//
//            if (itemId == R.id.Home && previousItem!="Home" ) {
//                actionBarTextView.text = "Home"
//
//                if (previousItem == "Profile"){
//                    previousItem = "Home"
//                    findNavController(R.id.navigationHostFragment).navigate(R.id.action_myProfileFragment_to_myHomeFragment)
//                }
//                else{
//                    previousItem = "Home"
//                    findNavController(R.id.navigationHostFragment).navigate(R.id.action_myOrdersFragment_to_myHomeFragment)
//
//
//                }
//
//            }
//
//
//            else if(itemId == R.id.Orders && previousItem !="Orders") {
//                //the else part is nothing but the orders part is clicked...
//                actionBarTextView.text = "Orders"
//                if (previousItem == "Home"){
//                    previousItem = "Orders"
//                    findNavController(R.id.navigationHostFragment).navigate(R.id.action_myHomeFragment_to_myOrdersFragment)
//                }
//                else{
//                    previousItem = "Orders"
//                    findNavController(R.id.navigationHostFragment).navigate(R.id.action_myProfileFragment_to_myOrdersFragment)
//
//                }
//            }
//
//            else if (itemId == R.id.Profile && previousItem !="Profile") {
//                actionBarTextView.text = "Profile"
//
//                if (previousItem == "Home"){
//                    previousItem = "Profile"
//                    findNavController(R.id.navigationHostFragment).navigate(R.id.action_myHomeFragment_to_myProfileFragment)
//
//                }
//                else{
//                    previousItem = "Profile"
//                    findNavController(R.id.navigationHostFragment).navigate(R.id.action_myOrdersFragment_to_myProfileFragment)
//
//
//                }
//
//            }
//
//            // It will help to replace the
//            // one fragment to other.
//
//            true
//        }



}
