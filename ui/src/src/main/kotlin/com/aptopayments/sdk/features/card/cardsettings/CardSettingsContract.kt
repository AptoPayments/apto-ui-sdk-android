package com.aptopayments.sdk.features.card.cardsettings

import com.aptopayments.core.data.card.CardDetails
import com.aptopayments.core.data.content.Content
import com.aptopayments.core.data.voip.Action
import com.aptopayments.sdk.core.platform.FragmentDelegate

interface CardSettingsContract {

    interface Delegate: FragmentDelegate {
        fun onBackFromCardSettings()
        fun showContentPresenter(content: Content, title: String)
        fun showMailComposer(recipient: String, subject: String?, body: String?)
        fun cardDetailsChanged(cardDetails: CardDetails?)
        fun transactionsChanged()
        fun askForBiometricAuthentication(title: String, description: String,
                                          onAuthSuccess: () -> Unit,
                                          onAuthFailure: () -> Unit,
                                          onAuthCancel: () -> Unit)
        fun onCardStateChanged()
        fun onSetPin()
        fun showVoip(action: Action)
        fun showStatement()
    }

    interface View {
        var delegate: Delegate?
    }
}
