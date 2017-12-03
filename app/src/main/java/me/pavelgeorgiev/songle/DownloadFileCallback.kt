package me.pavelgeorgiev.songle

interface DownloadFileCallback {
    fun downloadComplete(bytes: ByteArray, fileType: String)
    fun downloadFailed(errorMessage: String?, fileType: String)
}
