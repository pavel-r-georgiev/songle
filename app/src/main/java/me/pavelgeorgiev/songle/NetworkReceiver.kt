package me.pavelgeorgiev.songle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.support.v4.content.LocalBroadcastManager
import android.net.NetworkInfo
import me.pavelgeorgiev.songle.NetworkReceiver.NetworkStateReceiverListener







class NetworkReceiver() : BroadcastReceiver() {
    companion object {
        val NETWORK_AVAILABLE_ACTION = "me.pavelgeorgiev.NetworkAvailable"
        var IS_NETWORK_AVAILABLE = "isNetworkAvailable"

        /**
         * Checks if device is connected to the Internet
         */
        fun isNetworkConnected(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    private var listeners = HashSet<NetworkStateReceiverListener>();
    private var connected: Boolean? = null

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null || intent.extras == null)
            return

        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val ni = manager.activeNetworkInfo

        if (ni != null && ni.state == NetworkInfo.State.CONNECTED) {
            connected = true
        } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, java.lang.Boolean.FALSE)) {
            connected = false
        }

        notifyStateToAll()
    }

    private fun notifyStateToAll() {
        for (listener in listeners)
            notifyState(listener)
    }

    private fun notifyState(listener: NetworkStateReceiverListener?) {
        if (connected == null || listener == null)
            return

        if (connected === true)
            listener.networkAvailable()
        else
            listener.networkUnavailable()
    }

    fun addListener(l: NetworkStateReceiverListener) {
        listeners.add(l)
        notifyState(l)
    }

    fun removeListener(l: NetworkStateReceiverListener) {
        listeners.remove(l)
    }

    interface NetworkStateReceiverListener {
        fun networkAvailable()
        fun networkUnavailable()
    }
}
