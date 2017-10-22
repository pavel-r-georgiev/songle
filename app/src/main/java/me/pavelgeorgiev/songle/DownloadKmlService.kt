package me.pavelgeorgiev.songle

import android.content.Context
import android.os.AsyncTask
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.data.kml.KmlLayer
import java.io.IOException
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class DownloadKmlService(private val caller : DownloadKmlCallback) :
        AsyncTask<String, Void, ByteArray>() {

    val READ_TIMEOUT = 1000
    val CONNECTION_TIMEOUT = 15000
    lateinit var mStream : InputStream
    lateinit var output : ByteArray

    override fun doInBackground(vararg urls: String) : ByteArray {
        try {
            mStream = downloadUrl(urls[0])
            output = mStream.readBytes()
        } catch (e: IOException) {
            println("Unable to load content. Check your network connection.")
        }
        return output
    }


    @Throws(IOException::class)
    private fun downloadUrl(urlString: String) : InputStream {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpsURLConnection

        connection.readTimeout = READ_TIMEOUT
        connection.connectTimeout = CONNECTION_TIMEOUT
        connection.requestMethod = "GET"
        connection.doInput = true

        connection.connect()

        return connection.inputStream
    }

    override fun onPostExecute(result: ByteArray) {
        super.onPostExecute(result)
        caller.downloadComplete(result)
    }
}
