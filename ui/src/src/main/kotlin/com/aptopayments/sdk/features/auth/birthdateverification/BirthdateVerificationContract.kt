package com.aptopayments.sdk.features.auth.birthdateverification

import com.aptopayments.core.data.user.Verification
import com.aptopayments.sdk.core.platform.FragmentDelegate

interface BirthdateVerificationContract {

    interface View {
        var delegate: Delegate?
    }

    interface Delegate : FragmentDelegate {
        fun onBirthdateVerificationPassed(primaryCredentialVerification: Verification, birthdateVerification: Verification)
        fun onBackFromBirthdateVerification()
    }
}
