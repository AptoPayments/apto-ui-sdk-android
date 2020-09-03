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
)
