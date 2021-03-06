package com.aptopayments.sdk.features.disclaimer

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract

internal class DisclaimerViewModel(
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    init {
        analyticsManager.track(Event.Disclaimer)
    }
}
