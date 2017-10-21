package me.pavelgeorgiev.songle

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager

class NetworkReceiver : BroadcastReceiver() {

    private val WIFI = "Wi-Fi"
    private val ANY = "Any"
    private val URL = R.string.songs_xml_url

    // Whether there is a Wi-Fi connection.
    private val wifiConnected = false
    // Whether there is a mobile connection.
    private val mobileConnected = false
    // Whether the display should be refreshed.
    var refreshDisplay = true
    var sPref: String? = null

    override fun onReceive(context: Context, intent: Intent) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                        as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo

        if (sPref == WIFI && networkInfo?.type == ConnectivityManager.TYPE_WIFI) {
            // Wi´Fi is connected, so use Wi´Fi
            // returns to the app.
            refreshDisplay = true;
        } else if (sPref == ANY && networkInfo != null) {

            // Have a network connection and permission, so use data
        } else {
            // No Wi´Fi and no permission, or no network connection
        }
    }
}
