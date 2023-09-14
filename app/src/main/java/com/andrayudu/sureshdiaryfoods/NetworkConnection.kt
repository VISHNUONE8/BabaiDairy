package com.andrayudu.sureshdiaryfoods

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkInfo
import android.util.Log
import androidx.lifecycle.LiveData

@Suppress("DEPRECATION")
class NetworkConnection(context: Context):LiveData<Boolean>() {


    private lateinit var networkConnectionCallback: ConnectivityManager.NetworkCallback
    private val connectivityManager:ConnectivityManager=
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager


    //this is the function that runs in this Class (when any activity is observing this Class)
    override fun onActive() {
        super.onActive()
        updateNetworkConnection()
        connectivityManager.registerDefaultNetworkCallback(connectivityManagerCallback())
    }
    override fun onInactive() {
        super.onInactive()
        //here we are using try and catch because by the time we are calling unregister method
        //it has already been unregistered so it is throwing an error ,for further details see ExceptionDetails
        try {
            connectivityManager.unregisterNetworkCallback(connectivityManagerCallback())
        }
        catch (e:java.lang.Exception){
            Log.i("TAG", "the network callback was already unregistered$e")
        }
    }
    private fun connectivityManagerCallback(): ConnectivityManager.NetworkCallback {
        networkConnectionCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                //since we have extended LiveData to this class
                // all live data methods can be accessed anywhere
                postValue(true)
            }
            override fun onLost(network: Network) {
                super.onLost(network)
                postValue(false)
            }
        }
        return networkConnectionCallback
    }

    private fun updateNetworkConnection() {
        val activeNetworkConnection: NetworkInfo? = connectivityManager.activeNetworkInfo
        postValue(activeNetworkConnection?.isConnected == true)
    }
}
