package me.pavelgeorgiev.songle

/**
 * Interface for activities that download files through DownloadFileService
 */
interface DownloadFileCallback {
    fun downloadComplete(bytes: ByteArray, fileType: String)
    fun downloadFailed(errorMessage: String?, fileType: String)
}
