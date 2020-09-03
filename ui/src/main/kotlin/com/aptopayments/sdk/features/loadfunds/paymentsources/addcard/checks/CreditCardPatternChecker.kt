package com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.checks

import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.CardNetwork

internal class CreditCardPatternChecker {

    fun isValid(number: String, network: CardNetwork) =
        isPatternValid(number, network)

    private fun isPatternValid(
        cardNumber: String,
        network: CardNetwork
    ) = cardNumber.matches(network.pattern.toRegex())
}
