package com.aptopayments.sdk.features.card.fundingsources

import com.aptopayments.sdk.core.platform.FragmentDelegate

interface FundingSourceContract {

    interface Delegate : FragmentDelegate {
        fun onAddFundingSource(selectedBalanceID: String?)
        fun onFundingSourceSelected(onFinish: () -> Unit)
    }

    interface View {
        var delegate: Delegate?
    }
}
