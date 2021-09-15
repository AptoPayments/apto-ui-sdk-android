package com.aptopayments.sdk.features.card.activatephysicalcard.activate

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.card.ActivatePhysicalCardResult
import com.aptopayments.mobile.data.card.ActivatePhysicalCardResultType
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.exception.server.ServerErrorFactory
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent

internal class ActivatePhysicalCardViewModel(
    private val cardId: String,
    private val aptoPlatform: AptoPlatformProtocol,
    analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    val action = LiveEvent<Action>()

    init {
        analyticsManager.track(Event.ManageCardActivatePhysicalCard)
    }

    fun activatePhysicalCard(code: String) {
        showLoading()
        aptoPlatform.activatePhysicalCard(cardId, code) { result ->
            hideLoading()
            result.either({
                handleFailure(it)
            }) {
                when (it.result) {
                    ActivatePhysicalCardResultType.ACTIVATED -> action.postValue(Action.Activated)
                    ActivatePhysicalCardResultType.ERROR -> action.postValue(Action.Error(getError(it)))
                }
            }
        }
    }

    private fun getError(result: ActivatePhysicalCardResult): Failure.ServerError {
        val code = result.errorCode?.toInt()
        return ServerErrorFactory().create(code)
    }

    internal sealed class Action {
        object Activated : Action()
        class Error(val failure: Failure.ServerError) : Action()
    }
}
