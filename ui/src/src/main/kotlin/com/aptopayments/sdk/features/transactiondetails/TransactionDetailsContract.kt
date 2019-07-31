package com.aptopayments.sdk.features.transactiondetails

import com.aptopayments.sdk.core.platform.FragmentDelegate

interface TransactionDetailsContract {

    interface Delegate: FragmentDelegate {
        fun onTransactionDetailsBackPressed()
    }

    interface View {
        var delegate: Delegate?
    }
}
