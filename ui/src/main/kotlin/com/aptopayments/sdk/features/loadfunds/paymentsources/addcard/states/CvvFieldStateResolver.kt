package com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.states

import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.CardNetwork
import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.FieldState

internal class CvvFieldStateResolver {

    operator fun invoke(number: String?, cardNetwork: CardNetwork?): FieldState {
        val correctLength = cardNetwork?.cvvDigits ?: 0
        return when (number?.length ?: 0) {
            correctLength -> FieldState.CORRECT
            in 0 until correctLength -> FieldState.TYPING
            else -> FieldState.ERROR
        }
    }
}
