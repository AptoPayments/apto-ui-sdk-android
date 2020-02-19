package com.aptopayments.sdk.features.passcode

import com.aptopayments.core.analytics.Event
import com.aptopayments.sdk.core.usecase.VerifyPasscodeUseCase
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract

internal class ChangePasscodeViewModel(
    analyticsManager: AnalyticsServiceContract,
    private val verifyPasscodeUseCase: VerifyPasscodeUseCase
) : PasscodeViewModel(analyticsManager) {

    override fun viewLoaded() {
        analyticsManager.track(Event.ChangePasscodeStart)
    }

    override fun firstState() {
        assignState(CheckPasscodeState())
    }

    override fun configureSetState() {
        assignState(SetStateImpl())
    }

    override fun configureConfirmState(value: String) {
        assignState(ConfirmStateImpl(value))
    }

    override fun onBackFromSetState() {
        firstState()
    }

    inner class CheckPasscodeState : State {
        override fun init() {
            _showForgot.value = true
            setValues(getTitle(), "biometric_verify_pin_title_description")
        }

        override fun getTitle() = "biometric_verify_pin_title"

        override fun onPasscode(value: String) {
            if (isPasscodeCorrect(value)) {
                configureSetState()
            } else {
                wrongPin.value = true
            }
        }

        override fun onBack() {
            backpressed.value = true
        }

    }

    private inner class SetStateImpl : SetState() {
        override fun init() {
            super.init()
            _showForgot.value = false
        }

        override fun getTitle() = "biometric_change_pin_title"
    }

    private inner class ConfirmStateImpl(firstPin: String) : ConfirmState(firstPin) {
        override fun getTitle() = "biometric_change_pin_confirmation_title"
    }

    private fun isPasscodeCorrect(passcode: String): Boolean {
        return verifyPasscodeUseCase.run(passcode).either({ false }, { it }) as Boolean
    }

}
