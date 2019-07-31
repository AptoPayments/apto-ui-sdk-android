package com.aptopayments.sdk.features.maintenance

import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.core.analytics.Event
import javax.inject.Inject

internal class MaintenanceViewModel @Inject constructor(
        private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    fun viewLoaded() {
        analyticsManager.track(Event.Maintenance)
    }

}
