package com.aptopayments.sdk.features.disclaimer

interface DisclaimerContract {
    interface View {
        var delegate: Delegate?
    }

    interface Delegate {
        fun onDisclaimerAccepted()
        fun onDisclaimerDeclined()
    }
}
