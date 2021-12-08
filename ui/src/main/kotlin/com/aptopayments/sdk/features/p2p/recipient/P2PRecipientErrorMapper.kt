package com.aptopayments.sdk.features.p2p.recipient

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.exception.server.ErrorP2PSelfRecipient
import com.aptopayments.mobile.exception.server.ErrorRecipientNotFound

internal class P2PRecipientErrorMapper {
    fun invoke(failure: Failure): RecipientError {
        return when (failure) {
            is ErrorRecipientNotFound -> RecipientError.NOT_FOUND
            is ErrorP2PSelfRecipient -> RecipientError.SELF_RECIPIENT
            else -> RecipientError.NO_UI_ERROR
        }
    }
}
