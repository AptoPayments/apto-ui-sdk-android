package com.aptopayments.sdk.features.card.setpin

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract

internal class ConfirmCardPinViewModel(
    private val cardId: String,
    previousPin: String,
    analyticsManager: AnalyticsServiceContract,
    private val aptoPlatform: AptoPlatformProtocol
) : CardPinViewModel(analyticsManager, Event.ManageCardConfirmPin, previousPin = previousPin) {

    override val title = "manage_card_confirm_pin_title"
    override val description = "manage_card_confirm_pin_explanation"

    override fun correctPin(pin: String) {
        showLoading()
        aptoPlatform.changeCardPin(cardId, pin) { result ->
            result.either(::handleFailure) {
                hideLoading()
                postCorrectPin(pin)
            }
        }
    }
}
