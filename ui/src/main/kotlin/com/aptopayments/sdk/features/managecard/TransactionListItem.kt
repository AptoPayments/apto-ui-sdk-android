package com.aptopayments.sdk.features.managecard

import com.aptopayments.mobile.data.transaction.Transaction

internal sealed class TransactionListItem {
    object HeaderView : TransactionListItem()
    class SectionHeader(val title: String) : TransactionListItem()
    class TransactionRow(val transaction: Transaction) : TransactionListItem()

    fun itemType(): Int {
        return when (this) {
            is HeaderView -> HEADER_VIEW_TYPE
            is SectionHeader -> SECTION_HEADER_VIEW_TYPE
            is TransactionRow -> TRANSACTION_ROW_VIEW_TYPE
        }
    }

    companion object {
        const val HEADER_VIEW_TYPE = 0
        const val SECTION_HEADER_VIEW_TYPE = 1
        const val TRANSACTION_ROW_VIEW_TYPE = 2
    }
}
