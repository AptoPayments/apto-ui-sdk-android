package com.aptopayments.sdk.features.card.passcode.passcode

import android.os.Bundle
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.features.card.setpin.*
import com.aptopayments.sdk.features.card.setpin.CardPinFragment
import com.aptopayments.sdk.utils.MessageBanner
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val PIN_KEY = "PIN"
private const val CARD_ID = "CARD_ID"
private const val VERIFICATION_ID = "VERIFICATION_ID"

internal class ConfirmCardPasscodeFragment : CardPinFragment(), ConfirmCardPinContract.View {

    override val viewModel: ConfirmCardPasscodeViewModel by viewModel { parametersOf(cardId, pin, verificationId) }

    private lateinit var pin: String
    private lateinit var cardId: String
    private var verificationId: String? = null

    override var delegate: ConfirmCardPinContract.Delegate? = null

    override fun setUpArguments() {
        cardId = requireArguments()[CARD_ID] as String
        pin = requireArguments()[PIN_KEY] as String
        verificationId = requireArguments()[VERIFICATION_ID] as String?
    }

    override fun wrongPin() {
        delegate?.onBackFromPinConfirmation()
        notify(
            "manage_card_confirm_passcode_error_wrong_code_title".localized(),
            "manage_card_confirm_passcode_error_wrong_code_message".localized()
        )
    }

    override fun handleFailure(failure: Failure?) {
        super.handleFailure(failure)
        binding.pinView.text?.clear()
    }

    override fun correctPin(pin: String) {
        hideKeyboard()
        delegate?.pinConfirmed(pin)
        notify(
            "manage_card_confirm_passcode_success_title".localized(),
            "manage_card_confirm_passcode_success_message".localized(),
            MessageBanner.MessageType.SUCCESS
        )
    }

    override fun onBackPressed() {
        delegate?.onBackFromPinConfirmation()
    }

    companion object {
        fun newInstance(cardId: String, pin: String, verificationId: String?) = ConfirmCardPasscodeFragment().apply {
            arguments = Bundle().apply {
                putString(CARD_ID, cardId)
                putString(PIN_KEY, pin)
                putString(VERIFICATION_ID, verificationId)
            }
        }
    }
}
