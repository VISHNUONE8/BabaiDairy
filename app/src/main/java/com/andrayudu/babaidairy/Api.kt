package com.andrayudu.babaidairy

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface Api {

    @FormUrlEncoded
    @POST("sendToUsers")
    fun sendNotificationToUser(
        @Field("userToken")token:String,
        @Field("notifTitle")title:String,
        @Field("notifBody")body:String
    ):Call<ResponseBody>


    //posts a notification to the admin channel,"Mr.venu is requesting you to accept an order"
    //token field is not required at all...
    //title:alert
    @FormUrlEncoded
    @POST("send")
   fun  sendNotification(
        @Field("token")token:String,
        @Field("title")title:String,
        @Field("body")body:String
    ):Call<ResponseBody>

    @FormUrlEncoded
    @POST("/createorder")
    suspend fun createOrder(
        @Field("orderid") merchantOrderId: String,
        @Field("amount") amount: Double
    ): Response<Map<String, Any>>



}