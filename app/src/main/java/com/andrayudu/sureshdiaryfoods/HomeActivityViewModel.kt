package com.andrayudu.sureshdiaryfoods

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrayudu.sureshdiaryfoods.db.CartItemRepository
import com.andrayudu.sureshdiaryfoods.model.OrderModel
import com.andrayudu.sureshdiaryfoods.model.UserRegisterModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeActivityViewModel(): ViewModel() {


    val tag = "HomeActivityViewModel"

    private val mAuth = FirebaseAuth.getInstance()
    private val userId = mAuth.currentUser?.uid



    fun userOrAdmin() {

        viewModelScope.launch(Dispatchers.IO) {
            if (userId != null) {
                val user = FirebaseDatabase.getInstance().getReference("Users").child(userId).get()
                    .await().getValue(UserRegisterModel::class.java)

                //if the user is an admin he will be subscribed to Admin Notif channel...
                if (user?.role.equals("Admin")) {
                    Log.i(tag, "the user logged in is an Admin..")
                    subscribeToAdminNotifications()
                }
                //if the user is a customer then he will be subscribed to SDF channel...
                else {
                    Log.i(tag, "the user logged in is a customer..")
                    subscribeToSDF()
                }
            }
        }
    }

    private fun subscribeToAdminNotifications() {
        Firebase.messaging.subscribeToTopic("notifications")
            .addOnCompleteListener{task->
                var msg = "Subscribed"
                if (!task.isSuccessful){
                    msg = "Subscribe failed"
                }
                Log.i(tag,"The status of admins Subscription to notifications:${msg}")
            }
    }

    private fun subscribeToSDF() {
        Firebase.messaging.subscribeToTopic("sureshDairyFoods")
            .addOnCompleteListener{task->
                var msg = "Subscribed"
                if (!task.isSuccessful){
                    msg = "Subscribe failed"
                }
                Log.i(tag,"The status of customers Subscription to SDF notifications:${msg}")
            }
    }



}