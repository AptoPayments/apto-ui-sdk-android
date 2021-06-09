package com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.states

import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.FieldState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private const val ZIP_NUMBER = "123456"
private const val MAX_LENGTH = 5

internal class ZipFieldStateResolverTest {

    val sut = ZipFieldStateResolver()

    @Test
    fun `when field is null then Typing`() {
        val state = sut.invoke(null)

        assertEquals(FieldState.TYPING, state)
    }

    @Test
    fun `when field is empty then Typing`() {
        val state = sut.invoke(ZIP_NUMBER.take(0))

        assertEquals(FieldState.TYPING, state)
    }

    @Test
    fun `when field is almost max_length then Typing`() {
        val state = sut.invoke(ZIP_NUMBER.take(MAX_LENGTH - 1))

        assertEquals(FieldState.TYPING, state)
    }

    @Test
    fun `when field is max_length then Typing`() {
        val state = sut.invoke(ZIP_NUMBER.take(MAX_LENGTH))

        assertEquals(FieldState.CORRECT, state)
    }

    @Test
    fun `when field is bigger than max_length then Error`() {
        val state = sut.invoke(ZIP_NUMBER.take(MAX_LENGTH + 1))

        assertEquals(FieldState.ERROR, state)
    }
}
