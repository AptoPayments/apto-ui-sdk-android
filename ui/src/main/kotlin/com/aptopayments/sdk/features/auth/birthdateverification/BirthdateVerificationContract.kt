package com.aptopayments.sdk.features.auth.birthdateverification

import com.aptopayments.mobile.data.user.DataPoint
import com.aptopayments.sdk.core.platform.FragmentDelegate

internal interface BirthdateVerificationContract {

    interface View {
        var delegate: Delegate?
    }

    interface Delegate : FragmentDelegate {
        fun onBirthdateVerificationPassed(dataPoint: DataPoint)
        fun onBackFromBirthdateVerification()
    }
}
