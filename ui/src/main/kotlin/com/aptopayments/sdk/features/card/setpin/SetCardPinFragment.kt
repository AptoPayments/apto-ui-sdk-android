package com.aptopayments.sdk.features.card.setpin

import org.koin.androidx.viewmodel.ext.android.viewModel

internal class SetCardPinFragment : CardPinFragment(), SetCardPinContract.View {

    override var delegate: SetCardPinContract.Delegate? = null

    override val viewModel: SetCardPinViewModel by viewModel()

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
