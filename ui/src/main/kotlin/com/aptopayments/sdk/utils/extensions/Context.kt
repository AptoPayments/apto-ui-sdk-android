package com.aptopayments.sdk.utils.extensions

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

@SuppressLint("ServiceCast")
internal fun Context.copyToClipboard(value: String, label: String) {
    val myClipboard = this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    myClipboard.setPrimaryClip(ClipData.newPlainText(label, value))
}
