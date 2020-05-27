package com.aptopayments.sdk.features.card.waitlist

import androidx.lifecycle.MutableLiveData
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.card.Card
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract

internal class WaitlistViewModel constructor(
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    var card: MutableLiveData<Card> = MutableLiveData()

    fun getCard(cardId: String) {
        AptoPlatform.fetchFinancialAccount(accountId = cardId, forceRefresh = true) { result ->
            result.either(::handleFailure, card::postValue)
        }
    }

    fun viewLoaded() = analyticsManager.track(Event.Waitlist)
}
