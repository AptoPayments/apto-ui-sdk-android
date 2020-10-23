package com.aptopayments.sdk.features.loadfunds.add

import com.aptopayments.mobile.data.payment.Payment
import com.aptopayments.sdk.core.platform.FragmentDelegate

internal interface AddFundsContract {

    interface Delegate : FragmentDelegate {
        fun onPaymentResult(payment: Payment)
        fun onPaymentSourcesList()
        fun onAddPaymenSource()
        fun onBackFromAddFunds()
    }

    interface View {
        var delegate: Delegate?
    }
}
