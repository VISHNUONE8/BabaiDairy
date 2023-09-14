package com.andrayudu.sureshdiaryfoods.utililty

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import com.andrayudu.sureshdiaryfoods.R
import com.andrayudu.sureshdiaryfoods.model.ProductionReportModel
import com.andrayudu.sureshdiaryfoods.model.TokenSavingModel
import com.andrayudu.sureshdiaryfoods.ui.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {




    override fun onNewToken(token: String) {

        //saving the token to our firebase realtime database...
        Log.i("TAG","the new token generated is:${token}")
        //this token is generated as a device token and not an account token
        //no matter if we change the login account it will not be generated
        //Called when a new token for the default Firebase project is generated.
        //This is invoked after app install when a token is first generated, and again if the token changes.
        saveToken(token)
        super.onNewToken(token)
    }

    private fun saveToken(token: String) {


        val mAuth = FirebaseAuth.getInstance()
        val user = mAuth.currentUser
        val userId = user?.uid

        val tokenSavingModel:TokenSavingModel = TokenSavingModel(token,user?.email,userId)
        if (userId!=null){
            val postTask = FirebaseDatabase.getInstance().getReference("UserTokens").child(userId)
                .setValue(tokenSavingModel)
            if (postTask.isSuccessful){
                Toast.makeText(this,"UserToken Saved Successfully",Toast.LENGTH_SHORT).show()
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.i("TAG","the notification received is"+message.notification?.title.toString())

        if (message.getNotification() != null) {
            // Since the notification is received directly from
            // FCM, the title and the body can be fetched
            // directly as below.
            showNotification(
                message.notification?.getTitle(),
                message.notification?.getBody()
            )
        }
    }

    override fun onMessageSent(msgId: String) {
        super.onMessageSent(msgId)
        Log.i("the message has been sent successfully","bro"+msgId)
    }
    @RequiresApi(Build.VERSION_CODES.S)
    @SuppressLint("MissingPermission")
    private fun showNotification(title: String?, body: String?) {


        val notificationManager = NotificationManagerCompat.from(this)
        val intent = Intent(this, ProductionReportModel::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        val channelId = "SureshDairy"
        val channelName = "DairyAppNotifications"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationChannel = NotificationChannel(channelId,channelName,importance)
        notificationManager.createNotificationChannel(notificationChannel)

        val builder = Notification.Builder(this,channelId)
            .setSmallIcon(R.drawable.calendar_icon)
            .setChannelId(channelId)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)
            .setContentText(title)

        val notification = builder.build()

        notificationManager.notify(0,notification)
    }


}