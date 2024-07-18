package com.andrayudu.babaidairy.ui

import android.app.Activity
import android.content.DialogInterface
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.andrayudu.babaidairy.NetworkConnection
import com.andrayudu.babaidairy.R
import com.andrayudu.babaidairy.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.common.IntentSenderForResultStarter
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability


class HomeActivity : AppCompatActivity() {

    private val TAG = "HomeActivity"

    private val viewModel: HomeActivityViewModel by viewModels()

    private val UPDATE_REQUEST_CODE = 101

    private lateinit var networkConnection:NetworkConnection

    private lateinit var appUpdateManager: AppUpdateManager


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

        appUpdateManager  = AppUpdateManagerFactory.create(this)
        checkUpdate()
        appUpdateManager.registerListener(appUpdateListener)



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
        val actionBarText = "Babai Dairy"
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


    private val updateLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        // handle callback
        if (result.data == null) return@registerForActivityResult
        if (result.resultCode == UPDATE_REQUEST_CODE) {
            Toast.makeText(this, "Downloading stated", Toast.LENGTH_SHORT).show()
            Log.i("updateLauncher","Downloading started..")
            if (result.resultCode != Activity.RESULT_OK) {
                Toast.makeText(this, "Downloading failed" , Toast.LENGTH_SHORT).show()
            }
            }
        }
    private val updateResultStarter =
        IntentSenderForResultStarter { intent, _, fillInIntent, flagsMask, flagsValues, _, _ ->
            val request = IntentSenderRequest.Builder(intent)
                .setFillInIntent(fillInIntent)
                .setFlags(flagsValues, flagsMask)
                .build()
            // launch updateLauncher
            updateLauncher.launch(request)
        }



    // Create a listener to track request state updates.
    val listener = InstallStateUpdatedListener { state ->
        // (Optional) Provide a download progress bar.
        if (state.installStatus() == InstallStatus.DOWNLOADING) {
            val bytesDownloaded = state.bytesDownloaded()
            val totalBytesToDownload = state.totalBytesToDownload()
            // Show update progress bar.
        }
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            Snackbar.make(
                binding.bottomNavigation,
                "New app is ready",
                Snackbar.LENGTH_INDEFINITE
            ).setAction("Restart") {
                appUpdateManager.completeUpdate()
            }.show()
        }
        // Log state or install the update.
    }
    private fun checkUpdate() {
        val appUpdateInfoTask = appUpdateManager?.appUpdateInfo
        appUpdateInfoTask?.addOnSuccessListener { appUpdateInfo ->
            // This example applies an flexible update. To apply a immediate update
            // instead, pass in AppUpdateType.IMMEDIATE
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                try {
                    appUpdateManager?.startUpdateFlowForResult(
                        // Pass the intent that is returned by 'getAppUpdateInfo()'.
                        appUpdateInfo,
                        // an activity result launcher registered via registerForActivityResult
                        updateResultStarter,
                        //pass 'AppUpdateType.FLEXIBLE' to newBuilder() for
                        // flexible updates.
                        AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build(),
                        // Include a request code to later monitor this update request.
                        UPDATE_REQUEST_CODE
                    )
                } catch (exception: IntentSender.SendIntentException) {
                    Toast.makeText(this, "Something wrong went wrong!", Toast.LENGTH_SHORT).show()
                }

            } else {
                Log.d(TAG, "No Update available")
            }
        }
    }

    private val appUpdateListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            Snackbar.make(
                binding.bottomNavigation,
                getString(R.string.new_app_ready),
                Snackbar.LENGTH_INDEFINITE
            ).setAction(getString(R.string.restart)) {
                appUpdateManager.completeUpdate()
            }.show()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(appUpdateListener)
    }


}

