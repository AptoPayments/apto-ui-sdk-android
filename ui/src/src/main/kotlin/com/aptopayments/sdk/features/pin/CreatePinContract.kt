package com.aptopayments.sdk.features.pin

import com.aptopayments.sdk.core.platform.FragmentDelegate

interface CreatePinContract {

    interface Delegate : FragmentDelegate {
        fun onPinSetCorrectly(pin: String)
        fun onBackPressed()
    }

    interface View {
        var delegate: Delegate?
    }

}
