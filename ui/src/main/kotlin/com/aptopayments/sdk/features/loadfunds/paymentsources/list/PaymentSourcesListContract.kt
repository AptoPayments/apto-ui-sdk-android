package com.aptopayments.sdk.features.loadfunds.paymentsources.list

import com.aptopayments.sdk.core.platform.FragmentDelegate

internal interface PaymentSourcesListContract {

    interface Delegate : FragmentDelegate {
        fun onClosePaymentSourcesList()
        fun newCardPressed()
    }

    interface View {
        var delegate: Delegate?
    }
}
