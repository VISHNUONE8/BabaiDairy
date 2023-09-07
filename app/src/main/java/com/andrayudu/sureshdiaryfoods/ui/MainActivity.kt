package com.andrayudu.sureshdiaryfoods.ui

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.andrayudu.sureshdiaryfoods.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)




        mAuth  = Firebase.auth

        val userId = mAuth.currentUser?.uid

        Handler().postDelayed(Runnable {

            if (userId?.isNotEmpty() == true){

                val intent = Intent(this, HomeActivity::class.java)
                finish()
                startActivity(intent)

            }
            else{
                startActivity(Intent(this,LoginActivity::class.java))
            }




        },2000)

    }
}