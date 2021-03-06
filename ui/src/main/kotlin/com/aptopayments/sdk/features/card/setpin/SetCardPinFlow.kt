package com.aptopayments.sdk.features.card.setpin

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable

private const val SET_PIN_TAG = "SetCardPinFragment"
private const val CONFIRM_PIN_TAG = "ConfirmCardPinFragment"

internal class SetCardPinFlow(
    val cardId: String,
    val onBack: () -> Unit,
    val onFinish: () -> Unit
) : Flow(), SetCardPinContract.Delegate, ConfirmCardPinContract.Delegate {

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = fragmentFactory.setPinFragment(SET_PIN_TAG)
        fragment.delegate = this
        setStartElement(element = fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(SET_PIN_TAG) as? SetCardPinContract.View)?.let {
            it.delegate = this
        }
        (fragmentWithTag(CONFIRM_PIN_TAG) as? SetCardPinContract.View)?.let {
            it.delegate = this
        }
    }

    //
    // Set Pin
    //
    override fun setPinFinished(pin: String) {
        val fragment = fragmentFactory.confirmPinFragment(cardId = cardId, pin = pin, CONFIRM_PIN_TAG)
        fragment.delegate = this
        push(fragment as BaseFragment)
    }

    override fun onCloseFromSetPin() = onBack.invoke()

    //
    // Confirm Pin
    //
    override fun onBackFromPinConfirmation() = popFragment()

    override fun pinConfirmed(pin: String) {
        onFinish.invoke()
    }
}
