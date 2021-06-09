package com.aptopayments.sdk.features.card.notificationpreferences

import com.aptopayments.mobile.data.user.notificationpreferences.NotificationChannel
import com.aptopayments.mobile.data.user.notificationpreferences.NotificationPreferences

internal fun NotificationPreferences.calculateSecondaryChannel(): NotificationChannel {
    return if (isEmailActive(this))
        NotificationChannel.EMAIL
    else
        NotificationChannel.SMS
}

private fun isEmailActive(it: NotificationPreferences) =
    it.preferences.firstOrNull()?.activeChannels?.get(NotificationChannel.EMAIL) != null
