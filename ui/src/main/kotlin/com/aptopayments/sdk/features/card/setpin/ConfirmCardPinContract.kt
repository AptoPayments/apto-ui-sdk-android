package com.aptopayments.sdk.features.card.setpin

import com.aptopayments.sdk.core.platform.FragmentDelegate

interface ConfirmCardPinContract {

    interface Delegate : FragmentDelegate {
        fun onBackFromPinConfirmation()
        fun pinConfirmed(pin: String)
    }

    interface View {
        var delegate: Delegate?
    }
}
