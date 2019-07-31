package com.aptopayments.sdk.features.auth.inputphone

import com.aptopayments.core.data.user.Verification
import com.aptopayments.sdk.core.platform.FragmentDelegate

interface InputPhoneContract {

    interface Delegate: FragmentDelegate {
        fun onPhoneVerificationStarted(verification: Verification)
        fun onBackFromInputPhone()
    }

    interface View {
        var delegate: Delegate?
    }
}
