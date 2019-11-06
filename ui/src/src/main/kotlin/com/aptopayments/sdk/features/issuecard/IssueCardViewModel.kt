package com.aptopayments.sdk.features.issuecard

import androidx.lifecycle.MutableLiveData
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.card.Card
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract

internal class IssueCardViewModel constructor(
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    var card: MutableLiveData<Card> = MutableLiveData()
    var issueCardErrorCode: MutableLiveData<Int> = MutableLiveData()

    fun viewLoaded() {
        analyticsManager.track(Event.IssueCard)
    }

    fun trackErrorCode(errorCode: Int?) {
        val error = Failure.ServerError(errorCode)
        when {
            error.isErrorInsufficientFunds() ->
                analyticsManager.track(Event.IssueCardInsufficientFunds, error.toJSonObject())
            error.isErrorBalanceValidationsInsufficientApplicationLimit() ->
                analyticsManager.track(Event.IssueCardInsufficientApplicationLimit, error.toJSonObject())
            error.isErrorBalanceValidationsEmailSendsDisabled() ->
                analyticsManager.track(Event.IssueCardEmailSendsDisabled, error.toJSonObject())
            else -> analyticsManager.track(Event.IssueCardUnknownError)
        }
    }

    fun issueCard(cardApplicationId: String) {
        AptoPlatform.issueCard(cardApplicationId) { result ->
            result.either(
                { failure -> if (failure is Failure.ServerError) issueCardErrorCode.postValue(failure.code) },
                { card.postValue(it) }
            )
        }
    }
}
