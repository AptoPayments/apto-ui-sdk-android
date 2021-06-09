package com.aptopayments.sdk.features.card.waitlist

import androidx.lifecycle.MutableLiveData
import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract

internal class WaitlistViewModel(
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    var card: MutableLiveData<Card> = MutableLiveData()

    init {
        analyticsManager.track(Event.Waitlist)
    }

    fun getCard(cardId: String) {
        AptoPlatform.fetchCard(cardId = cardId, forceRefresh = true) { result ->
            result.either(::handleFailure, card::postValue)
        }
    }
}
