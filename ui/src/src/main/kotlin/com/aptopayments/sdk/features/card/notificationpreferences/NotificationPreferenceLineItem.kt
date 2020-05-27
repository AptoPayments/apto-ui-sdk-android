package com.aptopayments.sdk.features.card.notificationpreferences

import com.aptopayments.core.data.user.notificationpreferences.NotificationGroup

class NotificationPreferenceLineItem(
    val group: NotificationGroup.Group,
    val isPrimaryChannelActive: Boolean,
    val isSecondaryChannelActive: Boolean
)
