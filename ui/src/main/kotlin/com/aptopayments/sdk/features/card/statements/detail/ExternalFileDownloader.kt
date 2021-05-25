package com.aptopayments.sdk.features.card.statements.detail

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.left
import com.aptopayments.mobile.functional.right

class ExternalFileDownloader(val context: Context) {

    fun download(fileName: String, url: String): Either<Failure, Unit> {
        val request: DownloadManager.Request = DownloadManager.Request(Uri.parse(url))
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?

        return if (downloadManager != null) {
            downloadManager.enqueue(request)
            Unit.right()
        } else {
            NoDownloadServiceFailure().left()
        }
    }

    private class NoDownloadServiceFailure : Failure.FeatureFailure()
}
