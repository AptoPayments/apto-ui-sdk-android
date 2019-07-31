package com.aptopayments.sdk.features.managecard

import com.aptopayments.core.data.transaction.Transaction

sealed class TransactionListItem {
    object HeaderView : TransactionListItem()
    class SectionHeader(val title: String) : TransactionListItem()
    class TransactionRow(val transaction: Transaction) : TransactionListItem()

    fun itemType(): Int {
        return when(this) {
            is HeaderView -> headerViewType
            is SectionHeader -> sectionHeaderViewType
            is TransactionRow -> transactionRowViewType
        }
    }

    companion object {
        const val headerViewType = 0
        const val sectionHeaderViewType = 1
        const val transactionRowViewType = 2
    }
}
