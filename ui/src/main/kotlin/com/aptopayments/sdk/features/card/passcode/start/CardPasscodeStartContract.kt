package com.aptopayments.sdk.features.card.passcode.start

import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.sdk.core.platform.FragmentDelegate

internal class CardPasscodeStartContract {

    interface Delegate : FragmentDelegate {
        fun onBackFromPasscodeStart()
        fun onStartedWithVerification(verification: Verification)
        fun onStartedWithoutVerification()
    }

    interface View {
        var delegate: Delegate?
    }
}
