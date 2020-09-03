package com.aptopayments.sdk.features.loadfunds.add

import com.aptopayments.mobile.data.payment.Payment
import com.aptopayments.sdk.core.platform.FragmentDelegate

internal interface AddFundsContract {

    interface Delegate : FragmentDelegate {
        fun onFundsAdded(payment: Payment)
        fun onPaymentSourceChange()
        fun onBackFromAddFunds()
    }

    interface View {
        var delegate: Delegate?
    }
}
