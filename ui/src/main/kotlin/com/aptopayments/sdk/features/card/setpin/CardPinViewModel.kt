package com.aptopayments.sdk.features.card.setpin

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent

private const val PIN_LONGITUDE = 4

internal abstract class CardPinViewModel(
    private val analyticsManager: AnalyticsServiceContract,
    private val event: Event,
    private val previousPin: String? = null
) : BaseViewModel(), PinViewModelInterface {

    val action = LiveEvent<Action>()

    fun trackEvent() {
        analyticsManager.track(event)
    }

    fun setPin(pin: String) {
        if (PIN_LONGITUDE == pin.length) {
            if (previousPin != null && previousPin != pin) {
                action.postValue(Action.WrongPin)
            } else {
                correctPin(pin)
            }
        }
    }

    protected open fun correctPin(pin: String) = postCorrectPin(pin)

    protected fun postCorrectPin(pin: String) {
        action.postValue(Action.CorrectPin(pin))
    }

    sealed class Action {
        class CorrectPin(val pin: String) : Action()
        object WrongPin : Action()
    }
}

interface PinViewModelInterface {
    val title: String
    val description: String
}
