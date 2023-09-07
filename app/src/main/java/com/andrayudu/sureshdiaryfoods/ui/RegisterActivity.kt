package com.andrayudu.sureshdiaryfoods.ui

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.ActivityRegisterBinding
import com.andrayudu.sureshdiaryfoods.model.SpecialPricesModel
import com.andrayudu.sureshdiaryfoods.model.UserRegisterModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding:ActivityRegisterBinding
    private lateinit var mAuth: FirebaseAuth
    private lateinit var userReference: DatabaseReference
    private lateinit var specialPricesReference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)
        mAuth = FirebaseAuth.getInstance()
        userReference = FirebaseDatabase.getInstance().getReference("Users")
        specialPricesReference = FirebaseDatabase.getInstance().getReference("SpecialPrices")



        binding.btnRegister.setOnClickListener({ view: View? ->
//            if (!validateUsername() or !validateEmail() or !validatePassword() or !validatePhoneno()) {
//                return@setOnClickListener
//            }
            createUser() //Firebase SignUp Method


        })



    }
    private fun createSpecialPrices(userId:String){
        val kovaPrice: String = binding.etRegKova.getText().toString()
        val gheePrice: String = binding.etRegGhee.getText().toString()
        val otherSweetsPrice: String = binding.etRegOtherSweets.getText().toString()
        specialPricesReference.child(userId).setValue(SpecialPricesModel(kovaPrice,gheePrice,otherSweetsPrice))


    }
    private fun createFirebaseDBuser(userId:String){
        val email: String = binding.etRegEmail.getEditText()?.getText().toString()
        val password: String = binding.etRegPass.getText().toString()
        //we do not need to save the password because we are resetting the password using firevase auth methods
        val mobile: String = binding.Phonenumber.getText().toString()
        val name: String = binding.fullName.getText().toString()
        val limit: String = binding.Limit.getText().toString()

        userReference.child(userId).setValue(UserRegisterModel(name,mobile,email,limit,userId))
        createSpecialPrices(userId)

    }
    private fun createUser() {
        Log.d("TAG", "createuser() is called")

        val email: String = binding.etRegEmail.getEditText()?.getText().toString()
        val password: String = binding.etRegPass.getText().toString()


        if (TextUtils.isEmpty(email)) {
            binding.etRegEmail.setError("Email cannot be empty")
            binding.etRegEmail.requestFocus()
        } else if (TextUtils.isEmpty(password)) {
            binding.etRegPass.setError("Password cannot be empty")
            binding.etRegPass.requestFocus()
        } else {
            // create or sign up the user
            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                    this
                ) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TAG", "createUserWithEmail:success")
                        Toast.makeText(this,"createUserWithEmail:success",Toast.LENGTH_SHORT).show()

                        val userId = mAuth.currentUser?.uid.toString()

                        createFirebaseDBuser(userId)
//                        startActivity(Intent(this,LoginActivity::class.java))
                        // Sign in success, update UI with the signed-in user's information
                        //                                FirebaseUser user = mAuth.getCurrentUser();// as the user is now logged in the user value is initialized to any value other than null
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