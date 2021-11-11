package com.aptopayments.sdk.features.p2p.recipient

import com.aptopayments.mobile.data.transfermoney.CardHolderData
import com.aptopayments.sdk.core.platform.FragmentDelegate

internal class P2pRecipientContract {
    interface Delegate : FragmentDelegate {
        fun onTransferRecipientBackPressed()
        fun onCardholderSelected(recipient: CardHolderData)
    }

    interface View {
        var delegate: Delegate?
    }
}
