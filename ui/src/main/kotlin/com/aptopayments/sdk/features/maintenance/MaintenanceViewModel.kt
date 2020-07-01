package com.aptopayments.sdk.features.maintenance

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract

internal class MaintenanceViewModel constructor(
    analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    init {
        analyticsManager.track(Event.Maintenance)
    }
}
