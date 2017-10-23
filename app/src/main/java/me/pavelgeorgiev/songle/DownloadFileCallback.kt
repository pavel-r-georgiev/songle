package me.pavelgeorgiev.songle

import com.google.maps.android.data.kml.KmlLayer
import java.io.InputStream

interface DownloadFileCallback {
    fun downloadComplete(result: ByteArray, fileType: String)
}
