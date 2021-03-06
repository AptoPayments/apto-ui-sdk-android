package com.aptopayments.sdk.features.card.setpin

import android.os.Bundle
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.extension.localized
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val PIN_KEY = "PIN"
private const val CARD_ID = "CARD_ID"

internal class ConfirmCardPinFragment : CardPinFragment(), ConfirmCardPinContract.View {

    override val viewModel: ConfirmCardPinViewModel by viewModel { parametersOf(cardId, pin) }

    private lateinit var pin: String
    private lateinit var cardId: String

    override var delegate: ConfirmCardPinContract.Delegate? = null

    override fun setUpArguments() {
        cardId = requireArguments()[CARD_ID] as String
        pin = requireArguments()[PIN_KEY] as String
    }

    override fun wrongPin() {
        delegate?.onBackFromPinConfirmation()
        notify(
            "manage_card_confirm_pin_error_wrong_code_title".localized(),
            "manage_card_confirm_pin_error_wrong_code_message".localized()
        )
    }

    override fun handleFailure(failure: Failure?) {
        super.handleFailure(failure)
        binding.pinView.text?.clear()
    }

    override fun correctPin(pin: String) {
        hideKeyboard()
        delegate?.pinConfirmed(pin)
    }

    override fun onBackPressed() {
        delegate?.onBackFromPinConfirmation()
    }

    companion object {
        fun newInstance(cardId: String, pin: String) = ConfirmCardPinFragment().apply {
            arguments = Bundle().apply {
                putString(CARD_ID, cardId)
                putString(PIN_KEY, pin)
            }
        }
    }
}
