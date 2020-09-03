package com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.states

import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.FieldState

private const val MAX_ZIP_LENGTH = 5

internal class ZipFieldStateResolver {

    operator fun invoke(value: String?): FieldState {
        return when (value?.length ?: 0) {
            MAX_ZIP_LENGTH -> FieldState.CORRECT
            in 0 until MAX_ZIP_LENGTH -> FieldState.TYPING
            else -> FieldState.ERROR
        }
    }
}
