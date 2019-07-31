package com.aptopayments.sdk.features.card.fundingsources

import com.aptopayments.core.data.fundingsources.Balance

sealed class FundingSourceListItem {
    class FundingSourceRow(val balance: Balance, var selected: Boolean) : FundingSourceListItem()
    object AddFundingSourceButton : FundingSourceListItem()

    fun itemType(): Int {
        return when(this) {
            is FundingSourceRow -> fundingSourceRowType
            is AddFundingSourceButton -> addFundingSourceViewType
        }
    }

    companion object {
        const val fundingSourceRowType = 0
        const val addFundingSourceViewType = 1
    }
}
