package com.aptopayments.sdk.features.card.notificationpreferences

import com.aptopayments.sdk.core.platform.FragmentDelegate

interface NotificationPreferencesContract {

    interface Delegate : FragmentDelegate {
        fun onBackFromNotificationsPreferences()
    }

    interface View {
        var delegate: Delegate?
    }
}
