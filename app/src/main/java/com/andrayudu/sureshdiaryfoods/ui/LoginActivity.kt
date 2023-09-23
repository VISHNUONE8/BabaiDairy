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
import com.andrayudu.sureshdiaryfoods.model.TokenSavingModel
import com.andrayudu.sureshdiaryfoods.utility.ProgressButton
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    lateinit var binding:ActivityLoginBinding
    lateinit var mAuth:FirebaseAuth
    //this is the loginbutton which has progressbar in it..
    lateinit var progressButton: ProgressButton

    private var email:String? = null
    private var password:String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)

        mAuth  = Firebase.auth

        binding.progressBtnLogin.setOnClickListener {

            val btnName = "LOGIN"
            progressButton = ProgressButton(this,it,btnName)
            progressButton.buttonActivated()
            //if the validation is done then loginuser method will be called
            if(validateInputs()){
                loginUser()
            }

        }
    }

    private fun validateInputs(): Boolean {

         email = binding.etEmail.text.toString()
         password = binding.etPassword.text.toString()
        if (TextUtils.isEmpty(binding.etEmail.text.toString())){
            binding.etEmail.setError("Email cannot be empty");
            binding.etEmail.requestFocus()
            progressButton.buttonFinished()
            return false
        }else if (TextUtils.isEmpty(binding.etPassword.text.toString())){
            binding.etPassword.setError("Password cannot be empty");
            binding.etPassword.requestFocus();
            progressButton.buttonFinished()
            return false
        }
        return true
    }

    private fun saveToken(token: String) {

        val mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        val userId = user?.uid

        val tokenSavingModel = TokenSavingModel(token,user?.email,userId)
        if (userId!=null){
            val postTask = FirebaseDatabase.getInstance().getReference("UserTokens").child(userId)
                .setValue(tokenSavingModel)
            if (postTask.isSuccessful){
                Toast.makeText(this,"UserToken Saved Successfully",Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun loginUser() {

        //firebase methods
        mAuth.signInWithEmailAndPassword(email!!,password!!).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this@LoginActivity,"User Login successfull",Toast.LENGTH_SHORT).show()

                    getToken()
                    finish()
                    startActivity(Intent(this, HomeActivity::class.java))

                } else {
                    progressButton.buttonFinished()
                    Toast.makeText(this@LoginActivity,"Log in Error: " + task.exception!!.message,Toast.LENGTH_SHORT).show()
                }
            }
    }

    //gets the token of the device and saves it to the token database folder
    private fun getToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener {
            if (it.isSuccessful){
                saveToken(it.result)
                Log.i("TAG","the token of this device is :"+it.result)

            }
        })
    }
}