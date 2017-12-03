package me.pavelgeorgiev.songle

interface DownloadXmlCallback {
    fun downloadComplete(result: List<Song>)
}
