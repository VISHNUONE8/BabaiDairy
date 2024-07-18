package com.andrayudu.babaidairy.ui

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import com.andrayudu.babaidairy.R
import android.text.TextWatcher
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.andrayudu.babaidairy.databinding.ActivityVerifyOtpactivityBinding
import com.andrayudu.babaidairy.model.ItemsCatalogueModel
import com.andrayudu.babaidairy.model.UserRegisterModel
import com.andrayudu.babaidairy.utility.Helpers
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import java.util.concurrent.TimeUnit


//As of now we have 3 actvities 1.main 2.verifyotp 3.sendOTP these 3 activities dont have a model(data class) so the only thing required is
//creating view model classes which includes enabling data binding

class VerifyOTPActivity : AppCompatActivity() {

    private lateinit var binding:ActivityVerifyOtpactivityBinding
    private lateinit var verifyOtpViewModel: VerifyOtpViewModel
    private lateinit var callbacks:PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var helpers: Helpers? = null


    //Firebase related
    private val mDb = FirebaseDatabase.getInstance()
    private  var userReference: DatabaseReference = mDb.getReference("UsersTesting")
    private  var specialPricesReference: DatabaseReference =  mDb.getReference("SpecialPricesList")
    private val firebaseAuth:FirebaseAuth = FirebaseAuth.getInstance()




    private lateinit var userId:String
    private var mobileNumber:String? = null
    var verificationId:String? = "null"


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_verify_otpactivity)
        verifyOtpViewModel = ViewModelProvider(this)[VerifyOtpViewModel::class.java]
        binding.viewModel = verifyOtpViewModel
        binding.lifecycleOwner = this
        binding.executePendingBindings()



        helpers = Helpers(this)
        mobileNumber = intent.getStringExtra("mobile")
        verificationId  = intent.getStringExtra("verificationId")

        binding.textMobile.setText(String.format("+91-%s",mobileNumber))


        initClickListeners()
        initCallbacks()
        setupOTPInputs()
        //when a user mistakenly presses back,it shouldn't ruin the registration process
        //so on pressing back we will display a toast message to the user...
        initBackDispatcher()






    }


    private fun initClickListeners() {


        binding.buttonVerify.setOnClickListener{v->
            helpers?.hideKeyboard(this)
            if(binding.inputCode1.text.trim().isEmpty()
                ||binding.inputCode2.text.trim().isEmpty()
                ||binding.inputCode3.text.trim().isEmpty()
                ||binding.inputCode4.text.trim().isEmpty()
                ||binding.inputCode5.text.trim().isEmpty()
                ||binding.inputCode6.text.trim().isEmpty()){


                Toast.makeText(applicationContext,"Please enter valid code",Toast.LENGTH_SHORT).show()
                return@setOnClickListener

            }

            val code = binding.inputCode1.text.toString()+
                    binding.inputCode2.text.toString()+
                    binding.inputCode3.text.toString()+
                    binding.inputCode4.text.toString()+
                    binding.inputCode5.text.toString()+
                    binding.inputCode6.text.toString()

            if(verificationId !=null){
                binding.progressBar.visibility = View.VISIBLE
                binding.buttonVerify.visibility = View.INVISIBLE

                //the entered code is fed as credential to the SignInWithPhoneAuthCredential()
                val credential = PhoneAuthProvider.getCredential(verificationId!!, code)

                signInWithPhoneAuthCredential(credential)


            }



        }
        binding.buttonSignup.setOnClickListener {
            helpers?.hideKeyboard(this)
            saveUsertoDB(userId)
        }
        binding.textResendOTP.setOnClickListener { v ->
            val options = PhoneAuthOptions.newBuilder(firebaseAuth)
                .setPhoneNumber("+91" + intent.getStringExtra("mobile")) // Phone number to verify
                .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                .setActivity(this) // Activity (for callback binding)
                .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        }



    }


    private fun initCallbacks(){
         callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.                 Log.d(TAG, "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)

                Toast.makeText(this@VerifyOTPActivity,"otp verification successfull",Toast.LENGTH_SHORT).show()
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.

                Toast.makeText(applicationContext,e.message,Toast.LENGTH_SHORT).show()
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                } else if (e is FirebaseAuthMissingActivityForRecaptchaException) {
                    // reCAPTCHA verification attempted with null Activity
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                newVerificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.

                verificationId = newVerificationId
                binding.progressBar.visibility =View.GONE
                Toast.makeText(applicationContext,"OTP sent",Toast.LENGTH_SHORT).show()

                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
//                    storedVerificationId = verificationId
//                    resendToken = token
            }
        }

    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->

                binding.progressBar.visibility = View.GONE
                binding.buttonVerify.visibility = View.VISIBLE
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    //if this is the users first time then,ask user to enter username and email
                    //check if the user already exists in the database or not..

                    val user = task.result?.user
                    userId = user?.uid.toString()
                    Log.d(TAG, ":success"+user?.uid)
                    Log.d(TAG, "signInWithCredential:success")

                    val database = FirebaseDatabase.getInstance().getReference("UsersTesting").child(userId)
                    database.addValueEventListener(object : ValueEventListener{
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()){
                                val intent  = Intent(applicationContext, HomeActivity::class.java)
                                intent.flags  = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                            else{
                                // The user doesnt exist in the database..
                                binding.verificationLinLayout.visibility = View.GONE
                                TransitionManager.beginDelayedTransition(binding.signUpLinLayout,AutoTransition())
                                binding.signUpLinLayout.visibility = View.VISIBLE

                            }
                        }


                        override fun onCancelled(error: DatabaseError) {
                        }

                    })


                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(applicationContext,"The verification code entered was invalid",Toast.LENGTH_SHORT).show()

                    }
                    // Update UI
                }
            }
    }

    private fun saveUsertoDB(userId:String){

        val emailId = binding.etEmail.text.toString()
        val userName = binding.etUserName.text.toString()
        val address = binding.etAddress.text.toString().trim()

        CoroutineScope(Dispatchers.IO).launch {
//            val transportCharges:Int = if (binding.checkbox.isChecked) binding.etTransportCharges.text.toString().toInt() else 0
            val role = "Customer"
            val userRegisterModel = UserRegisterModel()
            userRegisterModel.Name = userName
            userRegisterModel.Email = emailId
            userRegisterModel.Mobile = mobileNumber
            userRegisterModel.userId = userId
            userRegisterModel.deviceToken = null
            userRegisterModel.role = role
            userRegisterModel.address = address

            val userRegisterTask = userReference.child(userId).setValue(userRegisterModel)
            userRegisterTask.await()
            if (userRegisterTask.isSuccessful){
                createSpecialPrices(userId,userName)
            }
        }

    }

    private suspend fun createSpecialPrices(userId:String?,name:String?){

        withContext(Dispatchers.IO){
            if (userId!=null) {

                val getDemoPricesModel = FirebaseDatabase.getInstance().getReference("ItemsCatalogue")
                    .get().await().getValue(ItemsCatalogueModel::class.java)

                val itemsCatalogueModel = ItemsCatalogueModel()

                getDemoPricesModel?.let {
                    itemsCatalogueModel.itemsList = getDemoPricesModel.itemsList
                    itemsCatalogueModel.customerName = name
                    itemsCatalogueModel.updatedAt = System.currentTimeMillis().toString()
                }

                val specialpriceSetting = specialPricesReference.child(userId)
                    .setValue(itemsCatalogueModel)
                specialpriceSetting.addOnCompleteListener {task->
                    if (task.isSuccessful) {
                        Toast.makeText(this@VerifyOTPActivity,"Prices have been Updated Successfully",Toast.LENGTH_LONG).show()
                        val intent  = Intent(applicationContext, HomeActivity::class.java)
                        intent.flags  = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                }

            }

        }
    }

    private fun initBackDispatcher() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(
                    this@VerifyOTPActivity,
                    "This Action is not allowed!!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun setupOTPInputs() {

        binding.inputCode1.addTextChangedListener(object :TextWatcher{

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(!s.toString().trim().isEmpty()){
                    binding.inputCode2.requestFocus()
                }

            }
            override fun afterTextChanged(s: Editable?) {

            }

        })

        binding.inputCode2.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(!s.toString().trim().isEmpty()){
                    binding.inputCode3.requestFocus()
                }
                else if(s.toString().trim().isEmpty()){
                    binding.inputCode1.requestFocus()

                }
            }
            override fun afterTextChanged(s: Editable?) {

            }

        })
        binding.inputCode3.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(!s.toString().trim().isEmpty()){
                    binding.inputCode4.requestFocus()
                }
                else if(s.toString().trim().isEmpty()){
                    binding.inputCode2.requestFocus()

                }
            }
            override fun afterTextChanged(s: Editable?) {

            }

        })
        binding.inputCode4.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(!s.toString().trim().isEmpty()){
                    binding.inputCode5.requestFocus()
                }
                else if(s.toString().trim().isEmpty()){
                    binding.inputCode3.requestFocus()

                }
            }
            override fun afterTextChanged(s: Editable?) {

            }

        })
        binding.inputCode5.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(!s.toString().trim().isEmpty()){
                   binding.inputCode6.requestFocus()
                }

                else if(s.toString().trim().isEmpty()){
                    binding.inputCode4.requestFocus()

                }
            }
            override fun afterTextChanged(s: Editable?) {

            }

        })


        binding.inputCode6.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(!s.toString().trim().isEmpty()){
//                    binding.inputCode6.requestFocus()
                    //Do Nothing
                }

                else if(s.toString().trim().isEmpty()){
                    binding.inputCode5.requestFocus()

                }
            }
            override fun afterTextChanged(s: Editable?) {

            }

        })




    }



}