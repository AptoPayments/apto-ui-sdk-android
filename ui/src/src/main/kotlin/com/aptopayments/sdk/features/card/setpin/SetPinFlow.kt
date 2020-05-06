package com.aptopayments.sdk.features.card.setpin

import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.flow.Flow
import com.aptopayments.sdk.core.platform.flow.FlowPresentable

private const val SET_PIN_TAG = "SetPinFragment"
private const val CONFIRM_PIN_TAG = "ConfirmPinFragment"

internal class SetPinFlow (
        var cardId: String,
        var onBack: (Unit) -> Unit,
        var onFinish: (Unit) -> Unit
) : Flow(), SetPinContract.Delegate, ConfirmPinContract.Delegate {

    override fun init(onInitComplete: (Either<Failure, Unit>) -> Unit) {
        val fragment = fragmentFactory.setPinFragment(SET_PIN_TAG)
        fragment.delegate = this
        setStartElement(element = fragment as FlowPresentable)
        onInitComplete(Either.Right(Unit))
    }

    override fun restoreState() {
        (fragmentWithTag(SET_PIN_TAG) as? SetPinContract.View)?.let {
            it.delegate = this
        }
        (fragmentWithTag(CONFIRM_PIN_TAG) as? SetPinContract.View)?.let {
            it.delegate = this
        }
    }

    //
    // Set Pin
    //
    override fun setPinFinished(pin: String) {
        val fragment = fragmentFactory.confirmPinFragment(pin, CONFIRM_PIN_TAG)
        fragment.delegate = this
        push(fragment as BaseFragment)
    }

    override fun onCloseFromSetPin() = onBack(Unit)

    //
    // Confirm Pin
    //
    override fun onBackFromPinConfirmation() = popFragment()

    override fun pinConfirmed(pin: String) {
        showLoading()
        AptoPlatform.changeCardPin(cardId, pin) { result ->
            result.either(::handleFailure) {
                hideLoading()
                onFinish(Unit) }
        }
    }
}
