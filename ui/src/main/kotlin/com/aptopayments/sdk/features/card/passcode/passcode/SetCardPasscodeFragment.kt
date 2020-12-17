package com.aptopayments.sdk.features.card.passcode.passcode

import com.aptopayments.sdk.features.card.setpin.CardPinFragment
import com.aptopayments.sdk.features.card.setpin.SetCardPinContract

import org.koin.androidx.viewmodel.ext.android.viewModel

internal class SetCardPasscodeFragment : CardPinFragment(), SetCardPinContract.View {

    override var delegate: SetCardPinContract.Delegate? = null

    override val viewModel: SetCardPasscodeViewModel by viewModel()

    override fun wrongPin() {
        // Can't happen
    }

    override fun onBackPressed() {
        hideKeyboard()
        delegate?.onCloseFromSetPin()
    }

    override fun correctPin(pin: String) {
        delegate?.setPinFinished(pin)
    }
}
