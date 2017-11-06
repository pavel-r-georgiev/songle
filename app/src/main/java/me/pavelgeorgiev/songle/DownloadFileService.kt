package me.pavelgeorgiev.songle

import android.content.BroadcastReceiver
import android.os.AsyncTask
import java.io.IOException
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection


class DownloadFileService(private val caller : DownloadFileCallback, private val fileType : String) :
        AsyncTask<String, Void, ByteArray>() {

    companion object {
        val KML_TYPE = "KML"
        val TXT_TYPE = "TXT"
        val XML_TYPE = "XML"
    }

    val READ_TIMEOUT = 1000
    val CONNECTION_TIMEOUT = 15000
    lateinit var mStream : InputStream
    lateinit var output : ByteArray
    private var error = false
    private var errorMessage: String? = null

    override fun doInBackground(vararg urls: String) : ByteArray {
        try {
            mStream = downloadUrl(urls[0])
            output = mStream.readBytes()
        } catch (e: IOException) {
            error = true
            errorMessage = e.message
            println(errorMessage)
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
        if(!error) {
            caller.downloadComplete(result, fileType)
        } else {
            caller.downloadFailed(errorMessage, fileType)
        }
    }
}
