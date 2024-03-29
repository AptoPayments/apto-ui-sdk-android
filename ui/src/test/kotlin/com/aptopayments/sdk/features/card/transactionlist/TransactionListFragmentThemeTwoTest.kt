package com.aptopayments.sdk.features.card.transactionlist

import com.aptopayments.mobile.data.transaction.MCC
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.sdk.UnitTest
import org.mockito.kotlin.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module

class TransactionListFragmentThemeTwoTest : UnitTest() {
    // Collaborators
    private val viewModel: TransactionListViewModel = mock()
    private val cardId = "cardId"
    private val config = TransactionListConfig(startDate = null, endDate = null, mcc = MCC(name = null, icon = null))
    private lateinit var sut: TransactionListFragment

    @BeforeEach
    fun setUp() {
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
