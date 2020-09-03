package com.aptopayments.sdk.features.loadfunds.paymentsources.onboarding

import com.aptopayments.sdk.core.platform.FragmentDelegate

internal interface AddCardOnboardingContract {

    interface Delegate : FragmentDelegate {
        fun onBackAddCardOnboarding()
        fun onContinueAddCardOnboarding()
    }

    interface View {
        var delegate: Delegate?
    }
}
