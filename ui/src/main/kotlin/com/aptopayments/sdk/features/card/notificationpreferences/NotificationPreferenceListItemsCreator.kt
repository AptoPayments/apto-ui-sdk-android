package com.aptopayments.sdk.features.card.notificationpreferences

import com.aptopayments.mobile.data.user.notificationpreferences.NotificationChannel
import com.aptopayments.mobile.data.user.notificationpreferences.NotificationPreferences

internal class NotificationPreferenceListItemsCreator {

    fun create(
        preferences: NotificationPreferences
    ): List<NotificationPreferenceLineItem> {
        val secondaryChannel = preferences.calculateSecondaryChannel()
        return preferences.preferences.mapNotNull { notificationGroup ->
            val secondaryChannelActive = notificationGroup.activeChannels.isChannelActive(secondaryChannel)
            if (notificationGroup.groupId != null) {
                NotificationPreferenceLineItem(
                    notificationGroup.groupId!!,
                    notificationGroup.activeChannels.isChannelActive(NotificationChannel.PUSH),
                    secondaryChannelActive
                )
            } else {
                null
            }
        }
    }
}
