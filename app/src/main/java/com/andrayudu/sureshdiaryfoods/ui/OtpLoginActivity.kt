package com.andrayudu.sureshdiaryfoods.ui

import android.content.ContentValues
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.ActivityOtpLoginBinding
import com.andrayudu.sureshdiaryfoods.utility.Helpers
import com.andrayudu.sureshdiaryfoods.utility.ProgressButton
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class OtpLoginActivity : AppCompatActivity() {

    private val TAG = "OtpLoginActivity"
    private lateinit var otpLoginViewModel: OtpLoginViewModel
    private var helpers: Helpers? = null


    //UI components
    private lateinit var mAuth: FirebaseAuth
    private lateinit var binding: ActivityOtpLoginBinding
    private lateinit var progressButton: ProgressButton
    private lateinit var progressBtnLogin:View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        binding = DataBindingUtil.setContentView(this,R.layout.activity_otp_login)
        mAuth = Firebase.auth

        otpLoginViewModel = ViewModelProvider(this)[OtpLoginViewModel::class.java]
        binding.viewModel = otpLoginViewModel
        binding.lifecycleOwner = this

        helpers = Helpers(this)

        initClickListeners()
        initProgressBtn()


    }

    private fun initProgressBtn() {
        val btnName = "SEND OTP"
        progressBtnLogin = binding.progressBtnLogin
        progressButton = ProgressButton(this, progressBtnLogin, btnName)
    }
    private fun initClickListeners() {


        binding.progressBtnLogin.setOnClickListener{v->

            progressButton.buttonActivated()
            helpers?.hideKeyboard(this)
            //Here we should not make any method calls related to data coz we are using MVVM
            //but since we didn't know exactly how to make the separation of code we are proceeding like this....

            if(binding.inputMobile.text.toString().trim().isEmpty()){
                Toast.makeText(this,"EnterMobile", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }


            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken,
                ) {
                    // The SMS verification code has been sent to the provided phone number, we
                    // now need to ask the user to enter the code and then construct a credential
                    // by combining the code with a verification ID.
//                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this@OtpLoginActivity,"otp sent successfully ",Toast.LENGTH_SHORT).show()
                    val intent = Intent(applicationContext, VerifyOTPActivity::class.java)
                    intent.putExtra("mobile",binding.inputMobile.text.toString())
                    intent.putExtra("verificationId",verificationId)
                    intent.putExtra("mobile",binding.inputMobile.text.toString())
                    startActivity(intent)

                    Log.d(ContentValues.TAG, "onCodeSent:$verificationId")
                    //verification id is a long string ex: AL3R4eS3BeVewUE7kHUCcL3T6vEKuJxaZCB_ZrETRUI17baDWCfF1-v1fnzewx4aOY3lG6uyH8sfTmMYlrnn3JIdaWj3C5xQtgUsNFbqhiKpdshT6Rya7PSfmTO41nUnKjIfBIEg0RnaUi7GIwixtvJcpiN8pjJGCbFJed4oDtjJoocFAkB1IYsI5k1_kmwAtcEYNglyDWGsgc4LOJ75dKq2uPMorL6bKMLnevZPf540I0J4yWDnWwY
                    // Save verification ID and resending token so we can use them later
//                    storedVerificationId = verificationId
//                    resendToken = token
                }

                override fun onVerificationCompleted(p0: PhoneAuthCredential) {

                }

                override fun onVerificationFailed(p0: FirebaseException) {
                }
            }


            val options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber("+91" + binding.inputMobile.text.toString()) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this) // Activity (for callback binding)
                .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)

        }

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