package com.andrayudu.sureshdiaryfoods.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.ActivityPasswordResetBinding
import com.google.firebase.auth.FirebaseAuth

class PasswordResetActivity : AppCompatActivity() {

    private lateinit var binding:ActivityPasswordResetBinding
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = DataBindingUtil.setContentView(this, R.layout.activity_password_reset)

        mAuth = FirebaseAuth.getInstance()


        //for emails other than gmail,the reset mail is going into the spam folder
        binding.btnSubmit.setOnClickListener {
            val email =  binding.emailTV.getText().toString()
            resetPassword(email)
            println("the butotn is clicled:")
        }

    }

    private fun resetPassword(email:String) {

        println("inside ressetPassword()"+email)


        if (email != null && email.isNotEmpty()){
            mAuth.sendPasswordResetEmail(email).addOnCompleteListener{task->
                if (task.isSuccessful){
                    //this implies the link for resetting the password has been sent to the user
                    Toast.makeText(this,"Password Reset Email has been sent Successfully",Toast.LENGTH_LONG).show()

                    finish()


                }
                else{
                    //this means that the users mail id is wrong or there is any other error
                    Toast.makeText(this, task.exception?.message.toString(),Toast.LENGTH_LONG).show()


                }

            }

        }

    }
}