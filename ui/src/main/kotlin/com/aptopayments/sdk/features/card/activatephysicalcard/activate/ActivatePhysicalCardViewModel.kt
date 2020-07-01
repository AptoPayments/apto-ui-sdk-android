package com.aptopayments.sdk.features.card.activatephysicalcard.activate

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.card.ActivatePhysicalCardResult
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract

internal class ActivatePhysicalCardViewModel constructor(
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    fun activatePhysicalCard(cardId: String, code: String, onComplete: (ActivatePhysicalCardResult?) -> Unit) {
        AptoPlatform.activatePhysicalCard(cardId, code) { result ->
            result.either({
                handleFailure(it)
                onComplete(null)
            }) { onComplete(it) }
        }
    }

    fun viewLoaded() {
        analyticsManager.track(Event.ManageCardActivatePhysicalCard)
    }
}
