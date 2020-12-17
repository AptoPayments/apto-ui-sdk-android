package com.aptopayments.sdk.features.card.passcode.passcode

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable
import com.aptopayments.sdk.features.card.setpin.ConfirmCardPinContract
import com.aptopayments.sdk.features.card.setpin.SetCardPinContract

private const val SET_PASSCODE_TAG = "SetCardPasscode"
private const val CONFIRM_PASSCODE_TAG = "ConfirmCardPin"

internal class SetPasscodeFlow(
    val cardId: String,
    val verificationId: String?,
    val onFinish: () -> Unit,
    val onBack: () -> Unit
) : Flow(), SetCardPinContract.Delegate, ConfirmCardPinContract.Delegate {

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = fragmentFactory.setPasscodeFragment(SET_PASSCODE_TAG)
        fragment.delegate = this
        setStartElement(element = fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(SET_PASSCODE_TAG) as? SetCardPinContract.View)?.let {
            it.delegate = this
        }
        (fragmentWithTag(CONFIRM_PASSCODE_TAG) as? SetCardPinContract.View)?.let {
            it.delegate = this
        }
    }

    override fun setPinFinished(pin: String) {
        val fragment = fragmentFactory.confirmPasscodeFragment(cardId = cardId, pin = pin, verificationId = verificationId, tag = CONFIRM_PASSCODE_TAG)
        fragment.delegate = this
        push(fragment as BaseFragment)
    }

    override fun onCloseFromSetPin() = onBack.invoke()

    override fun onBackFromPinConfirmation() = popFragment()

    override fun pinConfirmed(pin: String) {
        onFinish.invoke()
    }
}
