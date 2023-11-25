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
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.andrayudu.sureshdiaryfoods.HomeActivityViewModel
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.databinding.FragmentProfileBinding
import com.andrayudu.sureshdiaryfoods.ui.*

class ProfileFragment : Fragment() {

    private val TAG = "ProfileFragment"

    //this viewmodel is common for all 4 frags and HomeActivity
    private val sharedViewModel: HomeActivityViewModel by activityViewModels()

    //UI related
    private lateinit var binding: FragmentProfileBinding
    private lateinit var mContext: Context


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
        initObservers()
        initClickListeners()

        sharedViewModel.callLoadUserData()

        return binding.root
    }

    private fun initClickListeners() {
        binding.relLayoutChangePassword.setOnClickListener {
            startActivity(Intent(mContext, PasswordResetActivity::class.java))
        }

        binding.relLayoutShippingPolicy.setOnClickListener {
            val intent = Intent(mContext,PdfViewingActivity::class.java)
            intent.putExtra("policyType","Shipping")
            startActivity(intent)
        }
        binding.relLayoutRefundPolicy.setOnClickListener {
            val intent = Intent(mContext,PdfViewingActivity::class.java)
            intent.putExtra("policyType","Return")
            startActivity(intent)
        }


        binding.relLayoutAbout.setOnClickListener {
            Toast.makeText(mContext,"WELCOME TO SDF, \nA TASTE OF  JOY",Toast.LENGTH_SHORT).show()
        }

        binding.relLayoutLogout.setOnClickListener {
            //show the alert dialog to confirm once again....
            showAlertDialog()
        }

    }

    private fun showAlertDialog() {

                val builder = AlertDialog.Builder(mContext)
                builder.setMessage("Are you Sure?")
                builder.setTitle("Logout !")
                builder.setCancelable(false)

                builder.setPositiveButton("Yes",(DialogInterface.OnClickListener { dialog, which ->
                    sharedViewModel.logOut()

                }))
                builder.setNegativeButton("No",(DialogInterface.OnClickListener { dialog, which ->
                    dialog.cancel()
                }))

                val alertDialog = builder.create()
                alertDialog.show()
    }

    private fun initObservers() {

        //EventObserver
        sharedViewModel.eventNotifyLiveData.observe(viewLifecycleOwner, Observer {
            it.getContentIfNotHandled()?.let {msg->
                if(msg == "Logout"){

                    requireActivity().finish()
                    val intent = Intent(mContext, LoginActivity::class.java)
                    startActivity(intent)
                    Toast.makeText(mContext, "User LogOut Success", Toast.LENGTH_SHORT).show()
                }
                else if (msg =="exception"){
                    Toast.makeText(mContext,"An Exception Occurred",Toast.LENGTH_SHORT).show()
                }

            }
        })

        sharedViewModel.userLive.observe(viewLifecycleOwner) { userDetails->

            userDetails?.let {
                binding.idPBLoading.visibility = View.GONE
                val role = userDetails.role

                binding.usernameTV.text = userDetails.Name
                binding.outstandingTV.text = getString(R.string.amount_display,("${userDetails.Outstanding}"))
                binding.roleTV.text = role
            }
        }
    }


}