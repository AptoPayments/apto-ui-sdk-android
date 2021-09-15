package com.aptopayments.sdk.features.directdeposit.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.fundingsources.AchAccountDetails
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent

internal class AchAccountDetailsViewModel(
    cardId: String,
    aptoPlatform: AptoPlatformProtocol,
    analytics: AnalyticsServiceContract
) : BaseViewModel() {

    private val _achAccountDetails = MutableLiveData<AchAccountDetails>()
    val achAccountDetails = _achAccountDetails as LiveData<AchAccountDetails>

    val actions = LiveEvent<Action>()

    init {
        fetchCardDetails(aptoPlatform, cardId)
        analytics.track(Event.AchAccountDetails)
    }

    private fun fetchCardDetails(
        aptoPlatform: AptoPlatformProtocol,
        cardId: String
    ) {
        showLoading()
        aptoPlatform.fetchCard(cardId, false) { result ->
            hideLoading()
            result.either(::handleFailure) { card ->
                card.features?.achAccount?.accountDetails?.let { _achAccountDetails.postValue(it) }
            }
        }
    }

    fun accountLongClick() {
        actions.postValue(Action.CopyValueToClipboard(achAccountDetails.value!!.accountNumber, "account"))
    }

    fun routingLongClick() {
        actions.postValue(Action.CopyValueToClipboard(_achAccountDetails.value!!.accountNumber, "routing"))
    }

    sealed class Action {
        class CopyValueToClipboard(val value: String, val label: String) : Action()
    }
}
