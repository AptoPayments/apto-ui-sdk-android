package com.aptopayments.sdk.features.card.fundingsources

import com.aptopayments.core.data.fundingsources.Balance

sealed class FundingSourceListItem {
    class FundingSourceRow(val balance: Balance, var selected: Boolean) : FundingSourceListItem()
    object AddFundingSourceButton : FundingSourceListItem()

    fun itemType(): Int {
        return when (this) {
            is FundingSourceRow -> FUNDING_SOURCE_ROW_TYPE
            is AddFundingSourceButton -> ADD_FUNDING_SOURCE_VIEW_TYPE
        }
    }

    companion object {
        const val FUNDING_SOURCE_ROW_TYPE = 0
        const val ADD_FUNDING_SOURCE_VIEW_TYPE = 1
    }
}
