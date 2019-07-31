package com.aptopayments.sdk.features.card.account

import com.aptopayments.core.analytics.Event
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import javax.inject.Inject

internal class AccountSettingsViewModel @Inject constructor(
        private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    fun viewLoaded() {
        analyticsManager.track(Event.AccountSettings)
    }

}
