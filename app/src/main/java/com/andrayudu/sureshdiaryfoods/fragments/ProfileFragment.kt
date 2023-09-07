package com.andrayudu.sureshdiaryfoods.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.FragmentProfileBinding
import com.andrayudu.sureshdiaryfoods.model.UserRegisterModel
import com.andrayudu.sureshdiaryfoods.ui.LoginActivity
import com.andrayudu.sureshdiaryfoods.ui.PasswordResetActivity
import com.andrayudu.sureshdiaryfoods.ui.RegisterActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var binding: FragmentProfileBinding
    private lateinit var mContext: Context


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    private fun logOut() {
        mAuth.signOut()
        activity?.finish()
        val intent = Intent(mContext, LoginActivity::class.java)
        startActivity(intent)
        Toast.makeText(mContext, "User LogOut Successful", Toast.LENGTH_SHORT).show()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)


        mAuth = Firebase.auth
        val userid = mAuth.currentUser?.uid

        isAdmin(userid)
        getUserData(userid)
        binding.relLayoutChangePassword.setOnClickListener {
            //we will launch the password reset activity
            //for forgot password also we will be using the same activity
            startActivity(Intent(mContext, PasswordResetActivity::class.java))
        }




        binding.relLayoutLogout.setOnClickListener {

            logOut()

        }

        return binding.root
    }

    private fun getUserData(userId:String?) {
        val userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId.toString())

        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val userRegisterModel = snapshot.getValue(UserRegisterModel::class.java)
                    binding.usernameTV.text = userRegisterModel?.Name
                    binding.limitTV.append("₹ ${userRegisterModel?.Limit}")
                }
            }
            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun isAdmin(userid:String?) {
        //if the user is an admin
        if (userid.equals("LcYIRtG0z4PuSI5tCdgRMUxaBjG3")){

            binding.RegisterUser.visibility = View.VISIBLE
            binding.emailOrMobileTV.text = "Admin"

        }
        else
            binding.emailOrMobileTV.text = "Customer"


        binding.RegisterUser.setOnClickListener {
            startActivity(Intent(mContext, RegisterActivity::class.java))
        }
    }

}