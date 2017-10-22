package me.pavelgeorgiev.songle

import java.io.InputStream

interface DownloadXmlCallback {
    fun downloadComplete(result: List<Song>)
}
