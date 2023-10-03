package com.andrayudu.sureshdiaryfoods.fragments

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.FragmentProfileBinding
import com.andrayudu.sureshdiaryfoods.ui.*

class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var mContext: Context
    private lateinit var profileFragViewModel: ProfileFragViewModel


    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        profileFragViewModel  = ViewModelProvider(this)[ProfileFragViewModel::class.java]




        initObservers()
        initClickListeners()

        profileFragViewModel.getUserData()

        return binding.root
    }

    private fun initClickListeners() {
        binding.relLayoutChangePassword.setOnClickListener {
            //we will launch the password reset activity
            //for forgot password also we will be using the same activity
            startActivity(Intent(mContext, PasswordResetActivity::class.java))
        }

        binding.relLayoutLogout.setOnClickListener {
            //show the alert dialog to confirm once again....
            showAlertDialog()

        }
        binding.relLayoutAdminPanel.setOnClickListener {
            startActivity(Intent(mContext,AdminPanelActivity::class.java))
        }
    }

    private fun showAlertDialog() {

                val builder = AlertDialog.Builder(mContext)
                builder.setMessage("Are you Sure?")
                builder.setTitle("Logout !")
                builder.setCancelable(false)

                builder.setPositiveButton("Yes",(DialogInterface.OnClickListener { dialog, which ->
                    profileFragViewModel.logOut()

                }))
                builder.setNegativeButton("No",(DialogInterface.OnClickListener { dialog, which ->
                    dialog.cancel()
                }))

                val alertDialog = builder.create()
                alertDialog.show()
    }

    private fun initObservers() {
        profileFragViewModel.getStatus().observe(viewLifecycleOwner, Observer {
                if(it.equals("Logout")){

                    requireActivity().finish()
                    val intent = Intent(mContext, LoginActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(mContext, "User LogOut Successful", Toast.LENGTH_SHORT).show()
                }

        })

        profileFragViewModel.getUserDetails().observe(viewLifecycleOwner, Observer {
            if(it!=null){
                binding.idPBLoading.visibility = View.GONE
                val role = it.role
                if (role.equals("Admin")){
                    binding.outstandingTV.visibility = View.INVISIBLE
                    binding.relLayoutAdminPanel.visibility = View.VISIBLE
                }
                binding.usernameTV.text = it.Name
                binding.outstandingTV.text = ("Outstanding:₹ ${it.Outstanding}")
                binding.roleTV.text = role
            }

        })
    }


}