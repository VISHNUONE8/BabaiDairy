package com.andrayudu.sureshdiaryfoods.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrayudu.sureshdiaryfoods.model.UserRegisterModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileFragViewModel:ViewModel() {

    private val mAuth = FirebaseAuth.getInstance()
    private val userId = mAuth.currentUser?.uid
    private val statusObserver = MutableLiveData<String>()
    private val userDetails = MutableLiveData<UserRegisterModel?>()


    fun getStatus(): LiveData<String> {
        return statusObserver
    }
    fun getUserDetails():LiveData<UserRegisterModel?>{
        return userDetails
    }



    fun getUserData() {


        viewModelScope.launch(Dispatchers.IO) {
            val userReference = FirebaseDatabase.getInstance().getReference("Users").child(userId!!)
            userReference.addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userRegisterModel = snapshot.getValue(UserRegisterModel::class.java)
                        userDetails.postValue(userRegisterModel)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        }
    }


    fun logOut() {
        mAuth.signOut()
        //if the logging out is successfull then we will post logout action
        if (mAuth.currentUser == null){
            statusObserver.postValue("Logout")
        }

    }
}