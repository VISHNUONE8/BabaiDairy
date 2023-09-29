package com.andrayudu.sureshdiaryfoods.ui

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.andrayudu.sureshdiaryfoods.R
import com.google.android.datatransport.runtime.scheduling.jobscheduling.SchedulerConfig.Flag
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


//in Android manifest the attribute noHistory is in enable for this activity
class MainActivity : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        setContentView(R.layout.activity_main)

        mAuth = Firebase.auth

        val userId = mAuth.currentUser?.uid

        if (userId?.isNotEmpty() == true) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)

        }


    }
}