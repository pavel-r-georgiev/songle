package me.pavelgeorgiev.songle

interface DownloadCallback {
    fun downloadComplete(result: List<Song>)
}
