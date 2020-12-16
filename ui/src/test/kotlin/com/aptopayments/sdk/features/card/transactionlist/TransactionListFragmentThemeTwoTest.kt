package com.aptopayments.sdk.features.card.transactionlist

import com.aptopayments.mobile.data.transaction.MCC
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.sdk.AndroidTest
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
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
    private lateinit var sut: TransactionListFragment

    @Before
    override fun setUp() {
        super.setUp()
        startKoin {
            modules(
                module {
                    viewModel { viewModel }
                }
            )
        }
        sut = TransactionListFragment.newInstance(cardId, config)
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
