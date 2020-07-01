package com.aptopayments.sdk.features.managecard

import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.sdk.core.platform.FragmentDelegate

interface ManageCardContract {

    interface Delegate : FragmentDelegate {
        fun onAccountSettingsTapped()
        fun onActivatePhysicalCardTapped(card: Card)
        fun onCardStatsTapped()
        fun onFundingSourceTapped(selectedBalanceID: String?)
        fun onCardSettingsTapped(card: Card)
        fun onTransactionTapped(transaction: Transaction)
        fun onBackFromManageCard()
    }

    interface View {
        var delegate: Delegate?
        fun refreshCardData()
        fun refreshBalance()
        fun refreshTransactions()
    }
}
