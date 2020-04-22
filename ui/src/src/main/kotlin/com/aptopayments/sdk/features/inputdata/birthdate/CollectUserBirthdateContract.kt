package com.aptopayments.sdk.features.inputdata.birthdate

import com.aptopayments.core.data.user.BirthdateDataPoint
import com.aptopayments.sdk.core.platform.FragmentDelegate

internal interface CollectUserBirthdateContract {

    interface View {
        var delegate: Delegate?
    }

    interface Delegate : FragmentDelegate {
        fun onBirthdateEnteredCorrectly(value: BirthdateDataPoint)
        fun onBackFromBirthdateVerification()
    }
}
