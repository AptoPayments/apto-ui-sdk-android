package com.aptopayments.sdk.features.loadfunds.result

import com.aptopayments.mobile.data.content.Content
import com.aptopayments.sdk.core.platform.FragmentDelegate

internal interface AddFundsResultContract {

    interface Delegate : FragmentDelegate {
        fun onBackFromAddFundsResult()
        fun onCardholderAgreement(agreement: Content)
    }

    interface View {
        var delegate: Delegate?
    }
}
