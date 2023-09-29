package com.andrayudu.sureshdiaryfoods.ui

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.ActivityRegisterBinding
import com.andrayudu.sureshdiaryfoods.model.SpecialPricesModel
import com.andrayudu.sureshdiaryfoods.model.UserRegisterModel
import com.andrayudu.sureshdiaryfoods.utility.ProgressButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging

class RegisterActivity : AppCompatActivity() {


    val tag = "RegisterActivity"

    private lateinit var binding:ActivityRegisterBinding
    //this is the registerbutton which has progressbar on it....
    lateinit var progressButton: ProgressButton
    private lateinit var mAuth: FirebaseAuth
    private lateinit var userReference: DatabaseReference
    private lateinit var specialPricesReference: DatabaseReference
    private lateinit var progressBarTV:TextView

    private lateinit var actionBarBackButton: ImageView
    private lateinit var actionBarTextView: TextView

    private var name:String? = null
    private var email:String? = null
    private var password:String? = null
    private var limit:String? = null
    private var outstanding:String? = null
    private var phoneNo:String? = null
    private var kovaPrice:String? = null
    private var gheePrice:String? = null
    private var otherSweetsPrice:String? = null
    private var tranportCharges:String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)

        actionBarBackButton = binding.actionbarRegister.findViewById(R.id.actionbar_Back)
        actionBarTextView = binding.actionbarRegister.findViewById(R.id.actionbar_Text)
        actionBarTextView.text = "RegisterUser"



        mAuth = FirebaseAuth.getInstance()
        userReference = FirebaseDatabase.getInstance().getReference("Users")
        specialPricesReference = FirebaseDatabase.getInstance().getReference("SpecialPrices")

        progressBarTV = binding.progressBtnRegister.findViewById(R.id.progressBtnText)
        progressBarTV.text = "Register"




        initClickListeners()

    }

    private fun initClickListeners() {


        binding.checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                binding.etTransportChargesLayout.visibility = if(isChecked) View.VISIBLE else View.GONE
        }

        binding.progressBtnRegister.setOnClickListener { view: View? ->

            val btnName = "REGISTER"
            progressButton = ProgressButton(this, view,btnName)
            progressButton.buttonActivated()

            if(validateInputs()){
                //if all the above validations are passed then we will call createUser()
                createUser() //Firebase SignUp Method
            }



        }

        actionBarBackButton.setOnClickListener {
            onBackPressedDispatcher.addCallback(this,object : OnBackPressedCallback(true){
                override fun handleOnBackPressed() {

                    finish()
                }
            })
            onBackPressedDispatcher.onBackPressed()
        }

    }

    private fun validateInputs(): Boolean {
        //all field values should be verified if they are empty
        //the fields like Email,PhoneNo,Password should be further verified using pattern checking


        name = binding.fullName.text.toString()
        email = binding.etRegEmail.text.toString()
        password = binding.etRegPass.text.toString()
        limit = binding.Limit.text.toString()
        outstanding = binding.outstanding.text.toString()
        phoneNo = binding.Phonenumber.text.toString()
        kovaPrice= binding.etRegKova.text.toString()
        gheePrice = binding.etRegGhee.text.toString()
        otherSweetsPrice = binding.etRegOtherSweets.text.toString()
        tranportCharges = binding.etTransportCharges.text.toString()



        if (TextUtils.isEmpty(binding.fullName.text.toString())) {
            binding.fullName.setError("Name cannot be empty");
            binding.fullName.requestFocus()
            progressButton.buttonFinished()
            return false
        }

        else if(validateEmail(email!!)!!.equals(false)){
            binding.etRegEmail.requestFocus()
            progressButton.buttonFinished()
            return false
       }

        else if (validatePassword(password!!)!!.equals(false))
       {
           binding.etRegPass.requestFocus()
           progressButton.buttonFinished()
           return false
       }
        else if (TextUtils.isEmpty(binding.Limit.text.toString())) {
            binding.Limit.setError("Limit cannot be empty");
            binding.Limit.requestFocus()
            progressButton.buttonFinished()
            return false
        }
        else if (TextUtils.isEmpty(outstanding)) {
            binding.outstanding.setError("this field cannot be empty");
            binding.outstanding.requestFocus()
            progressButton.buttonFinished()
            return false
        }
        else if (validatePhoneno(phoneNo!!)!!.equals(false)){
            binding.Phonenumber.requestFocus()
            progressButton.buttonFinished()
            return false
        }
        else if (TextUtils.isEmpty(binding.etRegKova.text.toString())) {
            binding.etRegKova.setError("this field cannot be empty");
            binding.etRegKova.requestFocus()
            progressButton.buttonFinished()
            return false
        } else if (TextUtils.isEmpty(binding.etRegGhee.text.toString())) {
            binding.etRegGhee.setError("this field cannot be empty");
            binding.etRegGhee.requestFocus()
            progressButton.buttonFinished()
            return false
        } else if (TextUtils.isEmpty(binding.etRegOtherSweets.text.toString())) {
            binding.etRegOtherSweets.setError("this field cannot be empty");
            binding.etRegOtherSweets.requestFocus()
            progressButton.buttonFinished()
            return false
        }
        //if the transportRequired checkbox is checked and no input entered in transport charges edittext box
        //then we will throw error
        else if(binding.checkbox.isChecked && TextUtils.isEmpty(tranportCharges)){
            binding.etTransportCharges.setError("this field cannot be empty")
            binding.etTransportCharges.requestFocus()
            progressButton.buttonFinished()
            return false
        }

        return true
    }

    fun validateEmail(email: String): Boolean? {
        val emailValidate: String = email
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return if (emailValidate.isEmpty()) {
            binding.etRegEmail.setError("Field cannot be Empty")
            false
        } else if (!emailValidate.matches(emailPattern.toRegex())) {
           binding.etRegEmail.setError("Invalid Email Address")
            false
        } else {
            binding.etRegEmail.setError(null)
            true
        }
    }

    fun validatePhoneno(phoneNo: String): Boolean? {
        val phonenoValidate: String = phoneNo
        val MobilePattern = "[0-9]{10}"
        return if (phonenoValidate.isEmpty()) {
            binding.Phonenumber.setError("Field cannot be Empty")
            false
        } else if (!phonenoValidate.matches(MobilePattern.toRegex())) {
            binding.Phonenumber.setError("Invalid phoneNo")
            false
        } else {
            binding.Phonenumber.setError(null)
            true
        }
    }

    fun validatePassword(password: String): Boolean? {
        val passwordValidate: String = password
        val passwordPattern = "^" +
                "(?=.*[0-9])" +  //at least 1 digit
                "(?=.*[a-z])" +  //at least 1 lower case letter
                "(?=.*[A-Z])" +  //at least 1 upper case letter
                "(?=.*[a-zA-Z])" +  //any letter
                "(?=.*[@#$%^&+=])" +  //at least 1 special character
                "(?=\\S+$)" +  //no white spaces
                ".{8,}" +  //at least 8 characters
                "$"
        return if (passwordValidate.isEmpty()) {
            binding.etRegPass.setError("Field cannot be Empty")
            false
        } else if (!passwordValidate.matches(passwordPattern.toRegex())) {
            binding.etRegPasswordLayout.error = "Password is weak"
            false
        } else {
            binding.etRegPasswordLayout.setError(null)
            true
        }
    }

    private fun createSpecialPrices(userId:String){
        val kovaPrice: String = binding.etRegKova.getText().toString()
        val gheePrice: String = binding.etRegGhee.getText().toString()
        val otherSweetsPrice: String = binding.etRegOtherSweets.getText().toString()
       val specialpriceSetting =  specialPricesReference.child(userId).setValue(SpecialPricesModel(kovaPrice,gheePrice,otherSweetsPrice))
        specialpriceSetting.addOnCompleteListener {

            if (it.isSuccessful){
                finish()
                startActivity(Intent(this,HomeActivity::class.java))
            }
        }
    }
    private fun saveUsertoDB(userId:String){

        val transportCharges:String = if (binding.checkbox.isChecked) binding.etTransportCharges.text.toString() else "0"
        val role = "Customer"
        userReference.child(userId).setValue(UserRegisterModel(name,email,limit,outstanding,phoneNo,transportCharges,userId,null,role))
        createSpecialPrices(userId)

    }
    private fun createUser() {

        if (TextUtils.isEmpty(email)) {
            binding.etRegEmail.setError("Email cannot be empty")
            binding.etRegEmail.requestFocus()
        } else if (TextUtils.isEmpty(password)) {
            binding.etRegPass.setError("Password cannot be empty")
            binding.etRegPass.requestFocus()
        } else {
            // create or sign up the user
            mAuth.createUserWithEmailAndPassword(email!!, password!!)
                .addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "createUserWithEmail:success")
                        Toast.makeText(this,"createUserWithEmail:success",Toast.LENGTH_SHORT).show()

                        val userId = mAuth.currentUser?.uid.toString()

                        //after the firebase registration is done, we will save all the signup details on that userId as folder name
                        saveUsertoDB(userId)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("TAG", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            this@RegisterActivity, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }


}