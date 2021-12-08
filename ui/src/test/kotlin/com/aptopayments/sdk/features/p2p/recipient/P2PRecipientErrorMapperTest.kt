package com.aptopayments.sdk.features.p2p.recipient

import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.exception.server.ErrorP2PSelfRecipient
import com.aptopayments.mobile.exception.server.ErrorRecipientNotFound
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class P2PRecipientErrorMapperTest {

    val sut = P2PRecipientErrorMapper()

    @Test
    internal fun `given ErrorRecipientNotFound when mapped then NOT_FOUND`() {
        val error = ErrorRecipientNotFound()

        val result = sut.invoke(error)

        assertEquals(RecipientError.NOT_FOUND, result)
    }

    @Test
    internal fun `given ErrorP2PSelfRecipient when mapped then SELF_RECIPIENT`() {
        val error = ErrorP2PSelfRecipient()

        val result = sut.invoke(error)

        assertEquals(RecipientError.SELF_RECIPIENT, result)
    }

    @Test
    internal fun `given generic error when mapped then NO_UI_ERROR`() {
        val error = Failure.ServerError(0)

        val result = sut.invoke(error)

        assertEquals(RecipientError.NO_UI_ERROR, result)
    }
}
