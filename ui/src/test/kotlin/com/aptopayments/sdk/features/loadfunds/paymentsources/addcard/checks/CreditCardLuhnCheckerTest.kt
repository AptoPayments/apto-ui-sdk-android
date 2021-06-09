package com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.checks

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CreditCardLuhnCheckerTest {

    private val sut = CreditCardLuhnChecker()

    @Test
    fun `when provided correct number then isValid`() {
        assertTrue(sut.isValid("4242424242424242"))
    }

    @Test
    fun `when provided incorrect number then isValid`() {
        assertFalse(sut.isValid("4242424242424241"))
    }
}
