package com.aptopayments.sdk.features.card.waitlist

import com.aptopayments.sdk.core.platform.FragmentDelegate

interface WaitlistContract {

    interface Delegate : FragmentDelegate {
        fun onWaitlistFinished()
    }

    interface View {
        var delegate: Delegate?
    }
}
