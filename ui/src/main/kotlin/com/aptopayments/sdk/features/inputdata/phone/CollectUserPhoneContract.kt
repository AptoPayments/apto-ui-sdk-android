package com.aptopayments.sdk.features.inputdata.phone

import com.aptopayments.mobile.data.user.PhoneDataPoint
import com.aptopayments.sdk.core.platform.FragmentDelegate

interface CollectUserPhoneContract {

    interface Delegate : FragmentDelegate {
        fun onPhoneEnteredCorrectly(value: PhoneDataPoint)
        fun onBackFromCollectPhone()
    }

    interface View {
        var delegate: Delegate?
    }
}
