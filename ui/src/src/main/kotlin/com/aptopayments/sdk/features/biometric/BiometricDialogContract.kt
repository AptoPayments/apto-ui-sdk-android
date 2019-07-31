package com.aptopayments.sdk.features.biometric

import com.aptopayments.sdk.core.platform.FragmentDelegate

interface BiometricDialogContract {

    interface Delegate : FragmentDelegate {
        fun onAuthNotAvailable()
        fun onAuthSuccess()
        fun onAuthFailure()
        fun onAuthCancelled()
    }

    interface View {
        var delegate: Delegate?
    }
}
