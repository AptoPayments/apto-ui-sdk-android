package com.aptopayments.sdk.features.nonetwork

import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.core.analytics.Event
import javax.inject.Inject

internal class NoNetworkViewModel @Inject constructor(
        private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    fun viewLoaded() {
        analyticsManager.track(Event.NoNetwork)
    }

}
