package com.aptopayments.sdk.features.card.cardsettings

import com.aptopayments.mobile.data.card.Disclaimer
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.data.voip.Action
import com.aptopayments.sdk.core.platform.FragmentDelegate

interface CardSettingsContract {

    interface Delegate : FragmentDelegate {
        fun onBackFromCardSettings()
        fun showContentPresenter(content: Content, title: String)
        fun transactionsChanged()
        fun onCardStateChanged()
        fun onSetPin()
        fun showVoip(action: Action)
        fun showStatement()
        fun onAddFunds()
        fun onSetCardPasscode()
        fun showAddFundsSelector()
        fun showAddFundsDisclaimer(disclaimer: Disclaimer?)
        fun showOrderPhysicalCard()
        fun transferMoney()
    }

    interface View {
        var delegate: Delegate?
    }
}
