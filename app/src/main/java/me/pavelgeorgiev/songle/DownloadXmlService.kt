package me.pavelgeorgiev.songle

import android.os.AsyncTask
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class DownloadXmlService(private val caller : DownloadXmlCallback) :
                         AsyncTask<String, Void, List<Song>>() {

    val READ_TIMEOUT = 1000
    val CONNECTION_TIMEOUT = 15000
    var mSongs : List<Song>? = null

    override fun doInBackground(vararg urls: String) : List<Song>? {
        try {
            mSongs = loadFromNetwork(urls[0])
        } catch (e: IOException) {
            println("Unable to load content. Check your network connection.")
        } catch (e: XmlPullParserException) {
            println("Error parsing XML.")
        }
        return mSongs
    }

    private fun loadFromNetwork(urlString: String) : List<Song>{
        val stream = downloadUrl(urlString)
        return  SongsXmlParser().parse(stream)
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

    override fun onPostExecute(result: List<Song>) {
        super.onPostExecute(result)
        caller.downloadComplete(result)
    }
}