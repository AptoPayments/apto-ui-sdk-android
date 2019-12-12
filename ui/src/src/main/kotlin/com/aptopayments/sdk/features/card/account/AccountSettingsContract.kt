package com.aptopayments.sdk.features.card.account

import com.aptopayments.sdk.core.platform.FragmentDelegate

interface AccountSettingsContract {
    interface View {
        var delegate: Delegate?
    }

    interface Delegate: FragmentDelegate {
        fun showNotificationPreferences()
        fun onAccountSettingsClosed()
        fun onMonthlyStatementTapped()
        fun onLogOut()
        fun onChangePasscodeTapped()
    }
}
