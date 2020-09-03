package com.aptopayments.sdk.features.card.cardsettings

import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.data.voip.Action
import com.aptopayments.sdk.core.platform.FragmentDelegate

interface CardSettingsContract {

    interface Delegate : FragmentDelegate {
        fun onBackFromCardSettings()
        fun showContentPresenter(content: Content, title: String)
        fun showMailComposer(recipient: String, subject: String?, body: String?)
        fun transactionsChanged()
        fun onCardStateChanged()
        fun onSetPin()
        fun showVoip(action: Action)
        fun showStatement()
        fun onAddFunds()
    }

    interface View {
        var delegate: Delegate?
    }
}
