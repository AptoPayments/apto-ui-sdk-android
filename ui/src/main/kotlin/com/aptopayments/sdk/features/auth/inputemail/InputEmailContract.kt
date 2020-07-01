package com.aptopayments.sdk.features.auth.inputemail

import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.sdk.core.platform.FragmentDelegate

interface InputEmailContract {
    interface Delegate : FragmentDelegate {
        fun onEmailVerificationStarted(verification: Verification)
        fun onBackFromInputEmail()
    }

    interface View {
        var delegate: Delegate?
    }
}
