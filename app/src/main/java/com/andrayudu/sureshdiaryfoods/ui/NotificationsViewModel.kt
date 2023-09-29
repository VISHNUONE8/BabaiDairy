package com.andrayudu.sureshdiaryfoods.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.andrayudu.sureshdiaryfoods.Api
import com.andrayudu.sureshdiaryfoods.model.UserRegisterModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class NotificationsViewModel:ViewModel() {


    private val tag = "NotificationsViewModel"
    private val outstandingUserList = ArrayList<UserRegisterModel>()
    private val outstandingUsersLive = MutableLiveData<List<UserRegisterModel>>()
    private val statusLive = MutableLiveData<String>()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://sureshdairyfoods-f8a5a.web.app/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val api: Api = retrofit.create(Api::class.java)



    fun getOutstandingLive():LiveData<List<UserRegisterModel>>{
        return outstandingUsersLive
    }
    fun getStatusLive():LiveData<String>{
        return statusLive
    }


     fun loadOutstandingUsers() {
         viewModelScope.launch {


             val usersRef = FirebaseDatabase.getInstance().getReference("Users")
             usersRef.addValueEventListener(object : ValueEventListener {
                 override fun onDataChange(snapshot: DataSnapshot) {
                     outstandingUserList.clear()
                     if (snapshot.exists()) {
                         for (userSnap in snapshot.children) {
                             val user = userSnap.getValue(UserRegisterModel::class.java)
                             val userOutstanding = user?.Outstanding?.toInt()
                             if (userOutstanding!! < 0) {
                                 outstandingUserList.add(user)
                             }
                         }
                         outstandingUsersLive.postValue(outstandingUserList)
                         Log.i(tag, "users with outstanding are:$outstandingUserList")
                     }
                 }

                 override fun onCancelled(error: DatabaseError) {
                 }
             })
         }
     }

    //on calling this function it should send a notification to all the users with their outstanding amounts ....
    fun sendNotif() {
        viewModelScope.launch (Dispatchers.IO){

          for (user in outstandingUserList){


              val deviceToken = (user.deviceToken)!!
              val title = "Alert"
              val body = "Hi,${user.Name},You have an outstanding Balance of ${user.Outstanding}.Please pay the balance to Enjoy seamless Order experience.."
              val call: Call<ResponseBody> = api.sendNotificationToUser(deviceToken, title,body)


              call.enqueue(object : retrofit2.Callback<ResponseBody> {
                  override fun onResponse(
                      call: Call<ResponseBody>,
                      response: Response<ResponseBody>
                  ) {

                      try {
                          statusLive.postValue("Successfull")
                      } catch (e: IOException) {
                          e.printStackTrace()
                      }

                  }

                  override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                      Log.i("Sorry bro api call", "failed")
                  }
              })

          }



        }
    }


}