package com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.states

import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.CardNetwork
import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.FieldState
import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.checks.CreditCardLuhnChecker
import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.checks.CreditCardPatternChecker

internal class CardNumberFieldStateResolver {

    private val creditCardPatternChecker = CreditCardPatternChecker()
    private val creditCardLuhnChecker = CreditCardLuhnChecker()

    operator fun invoke(number: String, type: CardNetwork): FieldState {
        return if (type != CardNetwork.UNKNOWN && creditCardPatternChecker.isValid(number, type)) {
            if (creditCardLuhnChecker.isValid(number)) {
                FieldState.CORRECT
            } else {
                FieldState.ERROR
            }
        } else {
            FieldState.TYPING
        }
    }
}
