package com.aptopayments.sdk.features.card.notificationpreferences

import com.aptopayments.mobile.data.user.notificationpreferences.NotificationGroup

internal class NotificationPreferenceLineItem(
    val group: NotificationGroup.Group,
    val isPrimaryChannelActive: Boolean,
    val isSecondaryChannelActive: Boolean
)
