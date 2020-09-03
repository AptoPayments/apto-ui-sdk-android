package com.aptopayments.sdk.features.loadfunds.paymentsources.addcard

import com.aptopayments.sdk.core.platform.FragmentDelegate

internal interface AddCardPaymentSourceContract {

    interface Delegate : FragmentDelegate {
        fun onCardAdded()
        fun onBackFromSaveCard()
    }

    interface View {
        var delegate: Delegate?
    }
}
