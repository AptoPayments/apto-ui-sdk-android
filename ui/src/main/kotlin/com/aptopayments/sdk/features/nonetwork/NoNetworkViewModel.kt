package com.aptopayments.sdk.features.nonetwork

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract

internal class NoNetworkViewModel(
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    init {
        analyticsManager.track(Event.NoNetwork)
    }
}
