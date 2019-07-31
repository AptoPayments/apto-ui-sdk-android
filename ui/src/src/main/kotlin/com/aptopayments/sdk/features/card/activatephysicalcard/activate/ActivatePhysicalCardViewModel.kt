package com.aptopayments.sdk.features.card.activatephysicalcard.activate

import com.aptopayments.core.data.card.ActivatePhysicalCardResult
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.core.analytics.Event
import javax.inject.Inject

internal class ActivatePhysicalCardViewModel @Inject constructor(
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
