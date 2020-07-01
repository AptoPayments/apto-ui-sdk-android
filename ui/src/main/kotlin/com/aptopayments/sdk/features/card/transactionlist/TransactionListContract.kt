package com.aptopayments.sdk.features.card.transactionlist

import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.sdk.core.platform.FragmentDelegate

interface TransactionListContract {
    interface Delegate : FragmentDelegate {
        fun onBackPressed()
        fun onTransactionTapped(transaction: Transaction)
    }

    interface View {
        var delegate: Delegate?
    }
}
