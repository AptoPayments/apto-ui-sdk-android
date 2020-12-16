package com.aptopayments.sdk.features.card.waitlist

import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract

internal class WaitlistViewModel(
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    var card: MutableLiveData<Card> = MutableLiveData()

    fun getCard(cardId: String) {
        AptoPlatform.fetchCard(cardId = cardId, forceRefresh = true) { result ->
            result.either(::handleFailure, card::postValue)
        }
    }

    fun viewLoaded() = analyticsManager.track(Event.Waitlist)
}
