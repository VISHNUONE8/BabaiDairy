package com.andrayudu.sureshdiaryfoods.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
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
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {


    private val TAG = "LoginActivity"

    private lateinit var mAuth:FirebaseAuth
    private var email:String? = null
    private var password:String? = null

    //UI components
    private lateinit var progressButton: ProgressButton
    private lateinit var progressBtnLogin:View
    private lateinit var binding:ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        mAuth  = Firebase.auth

        initProgressBtn()
        initClickListeners()
    }

    private fun initProgressBtn() {
        val btnName = "LOG IN"
        progressBtnLogin = binding.progressBtnLogin
        //initiating progressButton
        progressButton = ProgressButton(this, progressBtnLogin, btnName)
    }

    private fun initClickListeners() {
        binding.progressBtnLogin.setOnClickListener {

            hideKeyboard(this)
            progressButton.buttonActivated()
            //if the validation is done then loginuser method will be called
            if(validateInputs()){
                loginUser()
            }

        }
    }


    //checks whether email or password is empty...
    private fun validateInputs(): Boolean {

         email = binding.etEmail.text?.trim().toString()
         password = binding.etPassword.text?.trim().toString()

        if (TextUtils.isEmpty(email)){
            binding.etEmail.setError("Email cannot be empty");
            binding.etEmail.requestFocus()
            progressButton.buttonFinished()
            return false
        }else if (TextUtils.isEmpty(password)){
            binding.etPassword.setError("Password cannot be empty");
            binding.etPassword.requestFocus();
            progressButton.buttonFinished()
            return false
        }
        return true
    }

    //saving device token to UserTokens database in the background IOthread..
    private fun saveToken(token: String) {

        CoroutineScope(Dispatchers.IO).launch{
            val user = mAuth.currentUser
            val userId = user?.uid

            val tokenSavingModel = TokenSavingModel(token,user?.email,userId)
            if (userId!=null){
                val postTask = FirebaseDatabase.getInstance().getReference("UserTokens").child(userId)
                    .setValue(tokenSavingModel)
                val postTasktoUsers = FirebaseDatabase.getInstance().getReference("UsersTesting").child(userId)
                    .child("deviceToken").setValue(token)

                postTask.await()
                postTasktoUsers.await()

                if (postTask.isSuccessful && postTasktoUsers.isSuccessful) {
                    withContext(Dispatchers.Main){
                        progressButton.buttonFinished()
                        finish()
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        Toast.makeText(this@LoginActivity,"User Login Success",Toast.LENGTH_SHORT).show()
                        Log.i(TAG,"saving token is successful")
                    }

                }
            }

        }

    }


    //logs the user in using firebaseAuth signIn methodss...
    private fun loginUser() {
        try {
            //firebase methods
            mAuth.signInWithEmailAndPassword(email!!,password!!).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    getToken()


                } else {
                    progressButton.buttonFinished()
                    Toast.makeText(this@LoginActivity,"Log in Error: " + task.exception!!.message,Toast.LENGTH_SHORT).show()
                }
            }
        }catch (e:Exception){
            Log.e(TAG,"The exception is : ${e.message.toString()}")
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

    //hides the keyboard and this is invoked on clicking login button
    fun hideKeyboard(activity: Activity) {
        val imm: InputMethodManager =
            activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onStart() {
        super.onStart()
        val user = mAuth.currentUser


        //if there is an user already logged in,then it will go to HomeActivity
        if (user!=null) {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        else {
            //stays in the current activity to login
        }
    }

}