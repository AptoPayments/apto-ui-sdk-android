package com.aptopayments.sdk.features.card.transactionlist

import com.aptopayments.core.data.transaction.MCC
import com.aptopayments.core.data.transaction.Transaction
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.mockito.Mock

class TransactionListFragmentThemeTwoTest : AndroidTest() {
    // Collaborators
    @Mock
    private lateinit var viewModel: TransactionListViewModel
    private val cardId = "cardId"
    private val config = TransactionListConfig(startDate = null, endDate = null, mcc = MCC(name = null, icon = null))
    private lateinit var sut: TransactionListFragmentThemeTwo

    @Before
    override fun setUp() {
        super.setUp()
        startKoin {
            modules(module {
                viewModel { viewModel }
            })
        }
        sut = TransactionListFragmentThemeTwo.newInstance(cardId, config)
        sut.cardId = cardId
        sut.config = config
    }

    @Ignore
    @Test
    fun `on fragment presented call view model to fetch transactions`() {
        // Given
        doNothing().whenever(viewModel).fetchTransaction(
            eq(cardId), eq(config.startDate), eq(config.endDate),
            eq(config.mcc), TestDataProvider.anyObject()
        )

        // When
        sut.onPresented()

        // Then
        verify(viewModel).fetchTransaction(
            eq(cardId), eq(config.startDate), eq(config.endDate), eq(config.mcc),
            TestDataProvider.anyObject()
        )
    }

    @Test
    fun `on back pressed notify delegate`() {
        // Given
        val delegate = mock<TransactionListContract.Delegate>()
        sut.delegate = delegate

        // When
        sut.onBackPressed()

        // Then
        verify(delegate).onBackPressed()
    }

    @Test
    fun `on transaction tapped notify delegate`() {
        // Given
        val delegate = mock<TransactionListContract.Delegate>()
        sut.delegate = delegate
        val transaction = mock<Transaction>()

        // When
        sut.onTransactionTapped(transaction)

        // Then
        verify(delegate).onTransactionTapped(transaction)
    }
}
