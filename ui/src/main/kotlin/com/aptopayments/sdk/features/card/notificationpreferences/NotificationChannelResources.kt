package com.aptopayments.sdk.features.card.notificationpreferences

import com.aptopayments.mobile.data.user.notificationpreferences.NotificationChannel
import com.aptopayments.sdk.R

class NotificationChannelResources {

    fun getTitleText(secondaryChannel: NotificationChannel): String {
        return if (secondaryChannel == NotificationChannel.EMAIL) {
            "notification_preferences_send_push_email_title"
        } else {
            "notification_preferences_send_push_sms_title"
        }
    }

    fun getIconImage(secondaryChannel: NotificationChannel): Int {
        return if (secondaryChannel == NotificationChannel.EMAIL) {
            R.drawable.ic_notifications_mail
        } else {
            R.drawable.ic_notifications_sms
        }
    }
}
