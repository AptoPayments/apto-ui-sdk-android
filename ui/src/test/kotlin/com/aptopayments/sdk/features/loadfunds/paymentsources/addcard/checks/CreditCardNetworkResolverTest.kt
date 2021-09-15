package com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.checks

import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.network.ApiKeyProvider
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.CardNetwork
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private const val LOAD_FUNDS_TEST_CARD = "9400212999999996"

class CreditCardNetworkResolverTest {

    private lateinit var sut: CreditCardNetworkResolver

    private val apiKeyProvider: ApiKeyProvider = mock() {
        on { isCurrentEnvironmentPrd() } doReturn true
    }

    @BeforeEach
    fun setUp() {
        sut = createSut(apiKeyProvider)
    }

    @Test
    fun `whenever visa number sent then VISA is returned`() {
        val array = getSubsequencesOfList(TestDataProvider.provideVisaValidNumbers(), 1)

        testCards(array, CardNetwork.VISA)
    }

    @Test
    fun `whenever master number sent then MASTER is returned`() {
        val array = getMastercardValidStartingNumbers()

        testCards(array, CardNetwork.MASTERCARD)
    }

    @Test
    fun `whenever amex number sent then AMEX is returned`() {
        val array = getSubsequencesOfList(TestDataProvider.provideAmexValidNumbers(), 2)

        testCards(array, CardNetwork.AMEX)
    }

    @Test
    fun `whenever discover number sent then DISCOVER is returned`() {
        val array = getDiscoverValidStartingNumbers()

        testCards(array, CardNetwork.DISCOVER)
    }

    @Test
    fun `when one cardnetwork is not in the list then is not recognized`() {
        sut.setAllowedNetworks(listOf(Card.CardNetwork.MASTERCARD))

        assertEquals(CardNetwork.UNKNOWN, sut.getCardType(TestDataProvider.provideVisaValidNumbers().first()))
    }

    @Test
    fun `when system is in PRD then load funds test card is not recognized`() {
        sut.setAllowedNetworks(listOf(Card.CardNetwork.MASTERCARD))

        val result = sut.getCardType(LOAD_FUNDS_TEST_CARD)

        assertEquals(CardNetwork.UNKNOWN, result)
    }

    @Test
    fun `when system is in not PRD then load funds test card is recognized`() {
        val apiKeyProviderInSbx: ApiKeyProvider = mock() {
            on { isCurrentEnvironmentPrd() } doReturn false
        }
        val sut = createSut(apiKeyProviderInSbx)
        sut.setAllowedNetworks(listOf(Card.CardNetwork.MASTERCARD))

        val result = sut.getCardType(LOAD_FUNDS_TEST_CARD)

        assertEquals(CardNetwork.TEST, result)
    }

    private fun createSut(apiKeyProvider: ApiKeyProvider) = CreditCardNetworkResolver(apiKeyProvider)

    private fun testCards(array: List<String>, network: CardNetwork) {
        array.forEach {
            println("Testing Card $it that should be ${network.name} beginning")
            assertEquals(network, sut.getCardType(it))
        }
    }

    private fun getMastercardValidStartingNumbers(): List<String> {
        return TestDataProvider.provideMasterValidNumbers().flatMap {
            when {
                it.startsWith("2") -> getSubsequenceArray(it, 4)
                else -> getSubsequenceArray(it, 2)
            }
        }
    }

    private fun getDiscoverValidStartingNumbers(): List<String> {
        return TestDataProvider.provideDiscoverValidNumbers().flatMap {
            when {
                it.startsWith("6011") -> getSubsequenceArray(it, 4)
                it.startsWith("64") -> getSubsequenceArray(it, 3)
                it.startsWith("65") -> getSubsequenceArray(it, 2)
                it.startsWith("6011") -> getSubsequenceArray(it, 4)
                it.startsWith("622") -> getSubsequenceArray(it, 3)
                else -> getSubsequenceArray(it, 2)
            }
        }
    }

    private fun getSubsequencesOfList(cardNumberList: List<String>, startFrom: Int): List<String> {
        return cardNumberList.flatMap { getSubsequenceArray(it, startFrom) }
    }

    private fun getSubsequenceArray(str: String, startFrom: Int): List<String> {
        val output = mutableListOf<String>()
        if (str.isNotEmpty()) {
            for (i in startFrom..str.length) {
                output.add(str.take(i))
            }
        }
        return output
    }
}
