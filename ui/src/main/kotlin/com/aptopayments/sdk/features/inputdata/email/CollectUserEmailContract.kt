package com.aptopayments.sdk.features.inputdata.email

import com.aptopayments.mobile.data.user.EmailDataPoint
import com.aptopayments.sdk.core.platform.FragmentDelegate

interface CollectUserEmailContract {
    interface Delegate : FragmentDelegate {
        fun onEmailEnteredCorrectly(value: EmailDataPoint)
        fun onBackFromCollectEmail()
    }

    interface View {
        var delegate: Delegate?
    }
}
