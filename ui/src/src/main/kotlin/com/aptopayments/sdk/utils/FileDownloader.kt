package com.aptopayments.sdk.utils

import java.io.File
import java.io.FileOutputStream
import java.net.URL

internal interface FileDownloader {
    fun downloadFile(link: String, destinationFile: File)
}

internal class FileDownloaderImpl : FileDownloader {

    override fun downloadFile(link: String, destinationFile: File) {
        URL(link).openStream().use { input -> FileOutputStream(destinationFile).use { output -> input.copyTo(output) } }
    }
}
