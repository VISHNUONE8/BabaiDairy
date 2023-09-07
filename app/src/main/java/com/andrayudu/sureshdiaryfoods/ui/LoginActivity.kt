package com.andrayudu.sureshdiaryfoods.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {

    lateinit var binding:ActivityLoginBinding
    lateinit var mAuth:FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        mAuth  = Firebase.auth


        Log.i("TAG","user id is:"+ (mAuth.currentUser?.uid))


        binding.btnLogin.setOnClickListener {
            loginUser()
        }


    }

    private fun loginUser() {
        Log.i("TAG","loginuser() is running")
        //firebase methods
        val email = binding.etUsername.text.toString()
        val password = binding.etLoginPass.text.toString()

        if (TextUtils.isEmpty(email)){
            binding.etUsername.setError("Email cannot be empty");
            binding.etUsername.requestFocus();
        }else if (TextUtils.isEmpty(password)){
            binding.etLoginPass.setError("Password cannot be empty");
            binding.etLoginPass.requestFocus();
        }else{
            Log.i("TAG","entered inti the else loop")
            Log.i("TAG","email"+email)
            Log.i("TAG","password"+password)


            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.i("TAG","login successful bigiluu")
                    Toast.makeText(
                        this@LoginActivity,
                        "User logged in successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(Intent(this, HomeActivity::class.java))

                } else {
                    Log.i("TAG","login error")

                    Toast.makeText(
                        this@LoginActivity,
                        "Log in Error: " + task.exception!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }


    }
    }
}