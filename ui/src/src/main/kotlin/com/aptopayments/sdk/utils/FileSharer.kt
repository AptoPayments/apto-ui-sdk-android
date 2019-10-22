package com.aptopayments.sdk.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.aptopayments.sdk.BuildConfig
import java.io.File

interface FileSharer {
    fun shareFile(file: File, context: Context)
}

class FileSharerImpl() : FileSharer {

    override fun shareFile(file: File, context: Context) {
        val contentUri = FileProvider.getUriForFile(context, BuildConfig.LIBRARY_PACKAGE_NAME, file)
        contentUri?.also { uri ->
            val shareIntent = getShareIntent(context, uri)
            context.startActivity(Intent.createChooser(shareIntent, ""))
        }
    }

    private fun getShareIntent(context: Context, uri: Uri): Intent {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        shareIntent.setDataAndType(uri, context.contentResolver.getType(uri))
        return shareIntent
    }
}
