package com.andrayudu.sureshdiaryfoods.ui

import android.Manifest
import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.adapters.ToolbarBindingAdapter
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.andrayudu.sureshdiaryfoods.HomeActivityViewModel
import com.andrayudu.sureshdiaryfoods.HomeActivityViewModelFactory
import com.andrayudu.sureshdiaryfoods.NetworkConnection
import com.andrayudu.sureshdiaryfoods.fragments.HomeFragment
import com.andrayudu.sureshdiaryfoods.fragments.OrdersFragment
import com.andrayudu.sureshdiaryfoods.fragments.ProfileFragment
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.ActivityHomeBinding
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.db.FoodItemDatabase
import com.andrayudu.sureshdiaryfoods.model.CartItem
import com.andrayudu.sureshdiaryfoods.model.TokenSavingModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging

class HomeActivity : AppCompatActivity(),BottomNavigationView.OnNavigationItemSelectedListener {


    private lateinit var binding: ActivityHomeBinding
    private lateinit var navController: NavController
    private lateinit var homeActivityViewModel: HomeActivityViewModel


    private lateinit var actionBarTextView: TextView



    val homeFragment = HomeFragment()
    val ordersFragment = OrdersFragment()
    val profileFragment = ProfileFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_home)
        val dao = FoodItemDatabase.getInstance(application).cartItemDao
        val repository = CartItemRepository(dao)
        val factory = HomeActivityViewModelFactory(repository)
        homeActivityViewModel = ViewModelProvider(this, factory)[HomeActivityViewModel::class.java]


        actionBarTextView = findViewById(R.id.actionbar_Home_Text)
        actionBarTextView.text = "SureshDairyFoods"

//
        val networkConnection = NetworkConnection(applicationContext)

        networkConnection.observe(this, Observer {isConnected->
            //here 'it' represents the connection status logic value
            if (isConnected){
                binding.navigationHostFragment.visibility = View.VISIBLE
                binding.actionbar.visibility = View.VISIBLE
                binding.noInternetLayout.visibility= View.GONE
                Toast.makeText(this,"Internet Connected",Toast.LENGTH_SHORT).show()
            }
            else{
                binding.navigationHostFragment.visibility = View.GONE
                binding.actionbar.visibility = View.GONE
                binding.noInternetLayout.visibility= View.VISIBLE
                Toast.makeText(this,"No Internet Connection",Toast.LENGTH_SHORT).show()
            }
        })

        Firebase.messaging.subscribeToTopic("notifications")
            .addOnCompleteListener{task->
                var msg = "Subscribed"
                if (!task.isSuccessful){
                    msg = "Subscribe failed"
                }
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }




        binding.bottomNavigation.selectedItemId = R.id.Home
        binding.bottomNavigation.setOnItemSelectedListener (this)
        supportFragmentManager.beginTransaction().replace(
            R.id.navigationHostFragment
        , HomeFragment()
        )

        homeActivityViewModel.cartItems.observe(this, Observer {
//            updateCartUI(it)
        })

    }
//    private fun updateCartUI(cartItems: List<CartItem>?) {
//        if(cartItems!=null && cartItems.size > 0){
//            binding.cartView.visibility = View.VISIBLE
//            var price =0
//            var quantity = 0
//
//            for (cartItem in cartItems) {
//                price = price +( cartItem.Price.toInt() * cartItem.Quantity.toInt())
//                quantity = quantity + cartItem.Quantity.toInt()
//            }
//            tCartQuantity.setText(cartItems.size.toString())
//            tTotalCost.setText(getString(R.string.rupee_symbol) + price.toString())
//
//        }
//        else
//        {
//            binding.cartView.setVisibility(View.GONE)
//            tCartQuantity.text = "0"
//            tTotalCost.text = getString(R.string.rupee_symbol) + "0"
//        }
//
//    }




    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100){
            if(grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Toast.makeText(this, "Notification Permission Granted", Toast.LENGTH_SHORT).show()            }
        }
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
