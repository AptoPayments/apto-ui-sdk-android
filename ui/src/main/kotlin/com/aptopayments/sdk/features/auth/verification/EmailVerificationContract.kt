package com.aptopayments.sdk.features.auth.verification

import com.aptopayments.mobile.data.user.DataPoint
import com.aptopayments.sdk.core.platform.FragmentDelegate

interface EmailVerificationContract {
    interface Delegate : FragmentDelegate {
        fun onEmailVerificationPassed(dataPoint: DataPoint)
        fun onBackFromEmailVerification()
    }

    interface View {
        var delegate: Delegate?
    }
}
