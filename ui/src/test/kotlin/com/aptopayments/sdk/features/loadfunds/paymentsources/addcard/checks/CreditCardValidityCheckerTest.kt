package com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.checks

import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.CardNetwork
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CreditCardValidityCheckerTest {

    private val sut = CreditCardPatternChecker()

    @Test
    fun `all visa correct examples are marked as valid`() {
        val list = TestDataProvider.provideVisaValidNumbers()

        testCards(list, CardNetwork.VISA)
    }

    @Test
    fun `all visa wrong examples are marked as invalid`() {
        val list = TestDataProvider.provideVisaInValidPatternNumbers()

        testInvalidCards(list, CardNetwork.VISA)
    }

    @Test
    fun `all master correct examples are marked as valid`() {
        val list = TestDataProvider.provideMasterValidNumbers()

        testCards(list, CardNetwork.MASTERCARD)
    }

    @Test
    fun `all master wrong examples are marked as invalid`() {
        val list = TestDataProvider.provideMasterInValidPatternNumbers()

        testInvalidCards(list, CardNetwork.MASTERCARD)
    }

    @Test
    fun `all amex correct examples are marked as valid`() {
        val list = TestDataProvider.provideAmexValidNumbers()

        testCards(list, CardNetwork.AMEX)
    }

    @Test
    fun `all amex wrong examples are marked as invalid`() {
        val list = TestDataProvider.provideAmexInValidPatternNumbers()

        testInvalidCards(list, CardNetwork.AMEX)
    }

    @Test
    fun `all discover correct examples are marked as valid`() {
        val list = TestDataProvider.provideDiscoverValidNumbers()

        testCards(list, CardNetwork.DISCOVER)
    }

    @Test
    fun `all discover wrong examples are marked as invalid`() {
        val list = TestDataProvider.provideDiscoverInValidPatternNumbers()

        testInvalidCards(list, CardNetwork.DISCOVER)
    }

    private fun testCards(array: List<String>, network: CardNetwork) {
        array.forEach {
            println("Testing Card $it that should be ${network.name} valid")
            assertTrue(sut.isValid(it, network))
        }
    }

    private fun testInvalidCards(array: List<String>, network: CardNetwork) {
        array.forEach {
            println("Testing Card $it that should not be ${network.name} valid")
            assertFalse(sut.isValid(it, network))
        }
    }
}
