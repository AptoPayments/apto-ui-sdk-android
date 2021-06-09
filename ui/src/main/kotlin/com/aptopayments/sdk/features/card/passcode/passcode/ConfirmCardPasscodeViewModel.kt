package com.aptopayments.sdk.features.card.passcode.passcode

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.card.setpin.CardPinViewModel

internal class ConfirmCardPasscodeViewModel(
    private val cardId: String,
    previousPin: String,
    private val verificationId: String?,
    analyticsManager: AnalyticsServiceContract,
    private val aptoPlatform: AptoPlatformProtocol
) : CardPinViewModel(analyticsManager, Event.CardPasscodeConfirm, previousPin = previousPin) {

    override val title = "manage_card_set_passcode_confirm_title"
    override val description = "manage_card_set_passcode_confirm_description"

    override fun correctPin(pin: String) {
        showLoading()
        aptoPlatform.setCardPasscode(
            cardId = cardId,
            passcode = pin,
            verificationId = verificationId
        ) { result ->
            hideLoading()
            result.either({ handleFailure(it) }) {
                postCorrectPin(pin)
            }
        }
    }
}
