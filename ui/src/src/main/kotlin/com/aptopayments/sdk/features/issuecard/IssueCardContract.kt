package com.aptopayments.sdk.features.issuecard

import com.aptopayments.core.data.card.Card
import com.aptopayments.sdk.core.platform.FragmentDelegate

interface IssueCardContract {
    interface View {
        var delegate: Delegate?
        fun issueCard()
    }

    interface Delegate : FragmentDelegate {
        fun onCardIssuedSucceeded(card: Card)
        fun onCardIssuedFailed(errorCode: Int?)
    }
}
