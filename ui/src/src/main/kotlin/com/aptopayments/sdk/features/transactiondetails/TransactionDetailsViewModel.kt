package com.aptopayments.sdk.features.transactiondetails

import com.aptopayments.core.analytics.Event
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract

internal class TransactionDetailsViewModel constructor(
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    fun viewLoaded() {
        analyticsManager.track(Event.TransactionDetail)
    }

}
