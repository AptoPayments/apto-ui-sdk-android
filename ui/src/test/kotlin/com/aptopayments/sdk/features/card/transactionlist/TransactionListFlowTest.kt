package com.aptopayments.sdk.features.card.transactionlist

import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.transaction.MCC
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.aptopayments.sdk.features.transactiondetails.TransactionDetailsFragment
import org.mockito.kotlin.*
import org.mockito.kotlin.mock
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val LIST_TAG = "TransactionListFragment"
private const val DETAILS_TAG = "TransactionDetailsFragment"

class TransactionListFlowTest : UnitTest() {
    private lateinit var sut: TransactionListFlow

    // Collaborators
    private val mockFragmentFactory: FragmentFactory = mock()
    private val cardId = "cardId"
    private val config = TransactionListConfig(startDate = null, endDate = null, mcc = MCC(name = null, icon = null))

    @BeforeEach
    fun setUp() {
        UIConfig.updateUIConfigFrom(TestDataProvider.provideProjectBranding())
        startKoin {
            modules(
                module {
                    single { mockFragmentFactory }
                }
            )
        }
    }

    @Test
    fun `sut initialized with correct Theme instantiate fragment from the factory`() {
        // Given
        sut = TransactionListFlow(cardId = cardId, config = config, onBack = {})
        val fragmentTestDouble = transactionListFragmentTestDouble()
        given {
            mockFragmentFactory.transactionListFragment(cardId, config, LIST_TAG)
        }.willReturn(fragmentTestDouble)

        // When
        sut.init {}

        // Then
        verify(mockFragmentFactory).transactionListFragment(cardId, config, LIST_TAG)
        assertEquals(sut, fragmentTestDouble.delegate)
    }

    @Test
    fun `on back pressed in fragment call on back closure`() {
        // Given
        var onBackCalled = false
        sut = TransactionListFlow(cardId = cardId, config = config, onBack = { onBackCalled = true })

        // When
        sut.onBackPressed()

        // Then
        assertTrue { onBackCalled }
    }

    @Test
    fun `on transaction tapped instantiate fragment from the factory`() {
        // Given
        sut = TransactionListFlow(cardId = cardId, config = config, onBack = {})
        val transaction = mock<Transaction>()
        val fragmentTestDouble = transactionDetailsFragmentTestDouble()
        given {
            mockFragmentFactory.transactionDetailsFragment(transaction, DETAILS_TAG)
        }.willReturn(fragmentTestDouble)

        // When
        sut.onTransactionTapped(transaction)

        // Then
        verify(mockFragmentFactory).transactionDetailsFragment(transaction, DETAILS_TAG)
        assertEquals(sut, fragmentTestDouble.delegate)
    }

    private fun transactionListFragmentTestDouble(): TransactionListFragment {
        return spy<TransactionListFragment>().apply { TAG = LIST_TAG }
    }

    private fun transactionDetailsFragmentTestDouble(): TransactionDetailsFragment {
        return spy<TransactionDetailsFragment>().apply { TAG = DETAILS_TAG }
    }
}
