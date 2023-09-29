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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {


    private val tag = "LoginActivity"

    private lateinit var binding:ActivityLoginBinding
    private lateinit var mAuth:FirebaseAuth
    private var email:String? = null
    private var password:String? = null

    //UI components
    private lateinit var progressButton: ProgressButton




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        mAuth  = Firebase.auth

        initClickListeners()
    }

    private fun initClickListeners() {
        binding.progressBtnLogin.setOnClickListener {

            hideKeyboard(this)
            val btnName = "LOGIN"
            progressButton = ProgressButton(this,it,btnName)
            progressButton.buttonActivated()
            //if the validation is done then loginuser method will be called
            if(validateInputs()){
                loginUser()
            }

        }
    }


    //checks whether email or password is empty...
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

    //saving device token to UserTokens database in the background IOthread..
    private fun saveToken(token: String) {

        CoroutineScope(Dispatchers.IO).launch{
            val mAuth = FirebaseAuth.getInstance()
            val user = mAuth.currentUser
            val userId = user?.uid

            val tokenSavingModel = TokenSavingModel(token,user?.email,userId)
            if (userId!=null){
                val postTask = FirebaseDatabase.getInstance().getReference("UserTokens").child(userId)
                    .setValue(tokenSavingModel)
                val postTasktoUsers = FirebaseDatabase.getInstance().getReference("Users").child(userId)
                    .child("deviceToken").setValue(token)

                postTask.await()
                postTasktoUsers.await()

                if (postTask.isSuccessful && postTasktoUsers.isSuccessful) {
                    Log.i(tag,"saving token is successful")
                }
            }

        }

    }


    //logs the user in using firebaseAuth signIn methodss...
    private fun loginUser() {

        //firebase methods
        mAuth.signInWithEmailAndPassword(email!!,password!!).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    getToken()
                    finish()
                    startActivity(Intent(this, HomeActivity::class.java))
                    Toast.makeText(this@LoginActivity,"User Login successfull",Toast.LENGTH_SHORT).show()


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

}