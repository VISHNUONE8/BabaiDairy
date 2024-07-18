package com.andrayudu.babaidairy.ui

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import com.andrayudu.babaidairy.R
import com.andrayudu.babaidairy.databinding.ActivityPasswordResetBinding
import com.andrayudu.babaidairy.utility.ProgressButton
import com.google.firebase.auth.FirebaseAuth

class PasswordResetActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    //UI Components
    private lateinit var progressButton: ProgressButton
    private lateinit var progressBtnLogin: View
    private lateinit var actionBarBackButton: ImageView
    private lateinit var actionBarTextView: TextView
    private lateinit var binding:ActivityPasswordResetBinding



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        mAuth = FirebaseAuth.getInstance()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_password_reset)



        initViews()
        initBackButton()
        initProgressBtn()
        initClickListeners()

    }

    private fun initViews() {
        actionBarBackButton = binding.actionbarPasswordReset.findViewById(R.id.actionbar_Back)
        actionBarTextView = binding.actionbarPasswordReset.findViewById(R.id.actionbar_Text)
        actionBarTextView.text = "Password Reset"
    }

    private fun validateInputs(): Boolean {
        val email = binding.emailTV.text?.trim().toString()

        if(TextUtils.isEmpty(email)){
            binding.emailTV.apply {
                error = "This field cannot be empty"
                requestFocus()
            }
            progressButton.buttonFinished()
            return false
        }
        return true

    }

    //for emails other than gmail,the reset mail is going into the spam folder
    private fun initClickListeners() {
        binding.progressBtnSendLink.setOnClickListener {
            hideKeyboard(this)
            progressButton.buttonActivated()
            if(validateInputs()){
                resetPassword()
            }
        }
    }

    private fun initBackButton() {
        actionBarBackButton.setOnClickListener {
            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    finish()
                }
            })
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun initProgressBtn() {
        val btnName = "SEND LINK"
        progressBtnLogin = binding.progressBtnSendLink
        //initiating progressButton
        progressButton = ProgressButton(this, progressBtnLogin, btnName)
    }

    private fun resetPassword() {

        val email = binding.emailTV.text.toString()


        mAuth.sendPasswordResetEmail(email).addOnCompleteListener{task->
            if (task.isSuccessful){
                //this implies the link for resetting the password has been sent to the user
                progressButton.buttonFinished()
                Toast.makeText(this,"Password Reset Email has been sent Successfully",Toast.LENGTH_LONG).show()
                finish()


            }
            else{
                //this means that the users mail id is wrong or there is any other error
                progressButton.buttonFinished()
                Toast.makeText(this, task.exception?.message.toString(),Toast.LENGTH_LONG).show()


            }

    }

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