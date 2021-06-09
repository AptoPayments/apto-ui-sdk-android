package com.aptopayments.sdk.features.card.passcode.start

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent

internal class CardPasscodeStartViewModel(
    cardId: String,
    analyticsManager: AnalyticsServiceContract,
    private val aptoPlatform: AptoPlatformProtocol
) : BaseViewModel() {

    private var card: Card? = null

    init {
        analyticsManager.track(Event.CardPasscodeStart)
        aptoPlatform.fetchCard(cardId, false) { result -> result.runIfRight { card = it } }
    }

    val actions = LiveEvent<Action>()

    fun onContinueClicked() {
        showLoading()
        if (card?.features?.passcode?.isVerificationRequired == true) {
            startVerificationAndContinue()
        } else {
            actions.postValue(Action.StartedWithoutVerification)
        }
    }

    private fun startVerificationAndContinue() {
        aptoPlatform.startPrimaryVerification { result ->
            hideLoading()
            result.either(::handleFailure) {
                actions.postValue(Action.StartedWithVerification(it))
            }
        }
    }

    fun onCancelClicked() {
        actions.postValue(Action.Cancel)
    }

    sealed class Action {
        class StartedWithVerification(val verification: Verification) : Action()
        object StartedWithoutVerification : Action()
        object Cancel : Action()
    }
}
