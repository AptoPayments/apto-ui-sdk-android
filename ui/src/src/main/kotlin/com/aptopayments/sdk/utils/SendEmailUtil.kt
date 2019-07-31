package com.aptopayments.sdk.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri

class SendEmailUtil(private val targetEmail: String = "support@shiftpayments.com",
                    private val subject: String? = null,
                    private val body: String? = null) {

    fun execute(activity: Activity) {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:$targetEmail")
        subject?.let { emailIntent.putExtra(Intent.EXTRA_SUBJECT, it) }
        body?.let { emailIntent.putExtra(Intent.EXTRA_TEXT, it) }
        activity.startActivity(emailIntent)
    }
}
