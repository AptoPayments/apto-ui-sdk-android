package com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.states

import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.CardNetwork
import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.FieldState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

private const val NUMBER = "123456"

internal class CvvFieldStateResolverTest {

    private val network = CardNetwork.VISA
    val sut = CvvFieldStateResolver()

    @Test
    fun `when field is null then Typing`() {
        val state = sut.invoke(null, network)

        assertEquals(FieldState.TYPING, state)
    }

    @Test
    fun `when field is empty then Typing`() {
        val state = sut.invoke(NUMBER.take(0), network)

        assertEquals(FieldState.TYPING, state)
    }

    @Test
    fun `when field is almost cvvDigits then Typing`() {
        val state = sut.invoke(NUMBER.take(network.cvvDigits - 1), network)

        assertEquals(FieldState.TYPING, state)
    }

    @Test
    fun `when field is cvvDigits then Typing`() {
        val state = sut.invoke(NUMBER.take(network.cvvDigits), network)

        assertEquals(FieldState.CORRECT, state)
    }

    @Test
    fun `when field is bigger than cvvDigits then Error`() {
        val state = sut.invoke(NUMBER.take(network.cvvDigits + 1), network)

        assertEquals(FieldState.ERROR, state)
    }
}
