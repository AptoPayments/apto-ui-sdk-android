package com.aptopayments.sdk.features.card.transactionlist

import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.transaction.MCC
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.fragment.FragmentFactory
import com.nhaarman.mockitokotlin2.given
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransactionListFlowTest : UnitTest() {
    private lateinit var sut: TransactionListFlow

    // Collaborators
    private val mockFragmentFactory: FragmentFactory = mock()
    private val cardId = "cardId"
    private val config = TransactionListConfig(startDate = null, endDate = null, mcc = MCC(name = null, icon = null))
    private val tag = "TransactionListFragment"
    private val detailsTag = "TransactionDetailsFragment"

    @Before
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
            mockFragmentFactory.transactionListFragment(cardId, config, tag)
        }.willReturn(fragmentTestDouble)

        // When
        sut.init {}

        // Then
        verify(mockFragmentFactory).transactionListFragment(cardId, config, tag)
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
            mockFragmentFactory.transactionDetailsFragment(transaction, detailsTag)
        }.willReturn(fragmentTestDouble)

        // When
        sut.onTransactionTapped(transaction)

        // Then
        verify(mockFragmentFactory).transactionDetailsFragment(transaction, detailsTag)
        assertEquals(sut, fragmentTestDouble.delegate)
    }

    private fun transactionListFragmentTestDouble(): TransactionListFragmentTestDouble {
        val fragmentTestDouble = TransactionListFragmentTestDouble()
        fragmentTestDouble.TAG = tag
        return fragmentTestDouble
    }

    private fun transactionDetailsFragmentTestDouble(): TransactionDetailsFragmentTestDouble {
        val fragmentTestDouble = TransactionDetailsFragmentTestDouble()
        fragmentTestDouble.TAG = detailsTag
        return fragmentTestDouble
    }
}
