package com.aptopayments.sdk.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.left
import com.aptopayments.mobile.functional.right

internal class SendEmailUtil(
    private val targetEmail: String = "support@aptopayments.com",
    private val subject: String? = null,
    private val body: String? = null
) {

    fun execute(activity: Activity): Either<Failure, Unit> {
        return try {
            val emailIntent = buildIntent()
            activity.startActivity(emailIntent)
            Unit.right()
        } catch (e: ActivityNotFoundException) {
            NoEmailClientConfiguredFailure().left()
        }
    }

    private fun buildIntent(): Intent {
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$targetEmail")
            subject?.let<String, Unit> { putExtra(Intent.EXTRA_SUBJECT, it) }
            body?.let<String, Unit> { putExtra(Intent.EXTRA_TEXT, it) }
        }
    }
}

internal class NoEmailClientConfiguredFailure :
    Failure.FeatureFailure(errorKey = "account_settings_help_email_not_configured")
