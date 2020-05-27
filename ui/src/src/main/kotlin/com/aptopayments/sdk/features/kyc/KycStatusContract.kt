package com.aptopayments.sdk.features.kyc

import com.aptopayments.sdk.core.platform.FragmentDelegate

interface KycStatusContract {

    interface Delegate : FragmentDelegate {
        fun onKycPassed()
        fun onKycClosed()
    }

    interface View {
        var delegate: Delegate?
    }
}
