package com.aptopayments.sdk.features.passcode

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract

internal class CreatePasscodeViewModel(analyticsManager: AnalyticsServiceContract) :
    PasscodeViewModel(analyticsManager) {

    override fun viewLoaded() = analyticsManager.track(Event.CreatePasscodeStart)

    override fun firstState() {
        configureSetState()
    }

    override fun configureSetState() {
        assignState(SetStateImpl())
    }

    override fun configureConfirmState(value: String) {
        assignState(ConfirmStateImpl(value))
    }

    override fun onBackFromSetState() {
        configureSetState()
    }

    private inner class SetStateImpl : SetState() {
        override fun getTitle() = "biometric_create_pin_title"
    }

    private inner class ConfirmStateImpl(firstPin: String) : ConfirmState(firstPin) {
        override fun getTitle() = "biometric_create_pin_confirmation_title"
    }
}
