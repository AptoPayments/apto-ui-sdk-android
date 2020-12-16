package com.aptopayments.sdk.features.managecard

import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.CardStyle

data class CardInfo(
    val cardId: String,
    val lastFourDigits: String,
    val cardNetwork: Card.CardNetwork,
    val state: Card.CardState,
    val orderedStatus: Card.OrderedStatus,
    val cardStyle: CardStyle?,
    val cardHolder: String
) {
    companion object {
        fun fromCard(card: Card): CardInfo {
            return CardInfo(
                cardId = card.accountID,
                cardHolder = card.cardHolder,
                lastFourDigits = card.lastFourDigits,
                cardNetwork = card.cardNetwork,
                state = card.state,
                orderedStatus = card.orderedStatus,
                cardStyle = card.cardStyle
            )
        }
    }
}
