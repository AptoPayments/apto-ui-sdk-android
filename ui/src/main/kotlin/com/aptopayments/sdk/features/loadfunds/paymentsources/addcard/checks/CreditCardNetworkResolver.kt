package com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.checks

import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.network.ApiKeyProvider
import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.CardNetwork

internal class CreditCardNetworkResolver(private val apiKeyProvider: ApiKeyProvider) {

    private var networks = CardNetwork.values().asList()

    fun getCardType(cardNumber: String) =
        networks.firstOrNull { cardNumber.matches(it.networkPattern.toRegex()) } ?: CardNetwork.UNKNOWN

    fun setAllowedNetworks(list: List<Card.CardNetwork>) {
        networks = list.map { CardNetwork.valueOf(it.name) }
        if (!apiKeyProvider.isCurrentEnvironmentPrd()) {
            networks = networks.plusElement(CardNetwork.TEST)
        }
    }
}
