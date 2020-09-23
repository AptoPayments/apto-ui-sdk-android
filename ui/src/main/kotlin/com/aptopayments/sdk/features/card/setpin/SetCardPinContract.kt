package com.aptopayments.sdk.features.card.setpin

import com.aptopayments.sdk.core.platform.FragmentDelegate

interface SetCardPinContract {

    interface Delegate : FragmentDelegate {
        fun onCloseFromSetPin()
        fun setPinFinished(pin: String)
    }

    interface View {
        var delegate: Delegate?
    }
}
