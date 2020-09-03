package com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.states

import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.CardNetwork
import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.FieldState
import org.junit.Assert.assertEquals
import org.junit.Test

internal class CardNumberFieldStateResolverTest {
    private val network = CardNetwork.VISA
    private val card = TestDataProvider.provideVisaValidNumbers().first()

    private val sut = CardNumberFieldStateResolver()

    @Test
    fun `when field is empty then Typing`() {
        val state = sut.invoke(card.take(0), network)

        assertEquals(FieldState.TYPING, state)
    }

    @Test
    fun `when field is almost completed then Typing`() {
        val state = sut.invoke(card.take(network.cvvDigits - 1), network)

        assertEquals(FieldState.TYPING, state)
    }

    @Test
    fun `when field is complete then Typing`() {
        val state = sut.invoke(card, network)

        assertEquals(FieldState.CORRECT, state)
    }

    @Test
    fun `when field invalid then Error`() {
        val state = sut.invoke(TestDataProvider.provideVisaInValidNumbers().first(), network)

        assertEquals(FieldState.ERROR, state)
    }
}
