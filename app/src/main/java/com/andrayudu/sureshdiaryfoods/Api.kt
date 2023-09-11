package com.andrayudu.sureshdiaryfoods

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface Api {
    @FormUrlEncoded
    @POST("send")
   fun  sendNotification(
        @Field("token")token:String,
        @Field("title")title:String,
        @Field("body")body:String
    ):Call<ResponseBody>
}