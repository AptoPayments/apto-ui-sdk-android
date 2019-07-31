package com.aptopayments.sdk.features.card.transactionlist

import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.core.data.transaction.MCC
import com.aptopayments.core.data.transaction.Transaction
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.mock

class TransactionListFragmentThemeTwoTest : AndroidTest() {
    // Collaborators
    @Mock private lateinit var viewModel: TransactionListViewModel
    private val cardId = "cardId"
    private val config = TransactionListConfig(startDate = null, endDate = null, mcc = MCC(name = null, icon = null))
    private lateinit var sut: TransactionListFragmentThemeTwo

    @Before
    override fun setUp() {
        super.setUp()
        sut = TransactionListFragmentThemeTwo.newInstance(cardId, config)
        sut.viewModel = viewModel
        sut.cardId = cardId
        sut.config = config
    }

    @Test
    fun `on fragment presented call view model to fetch transactions`() {
        // When
        sut.onPresented()

        // Then
        verify(viewModel).fetchTransaction(eq(cardId), eq(config.startDate), eq(config.endDate), eq(config.mcc),
                TestDataProvider.anyObject())
    }

    @Test
    fun `on back pressed notify delegate`() {
        // Given
        val delegate = mock(TransactionListContract.Delegate::class.java)
        sut.delegate = delegate

        // When
        sut.onBackPressed()

        // Then
        verify(delegate).onBackPressed()
    }

    @Test
    fun `on transaction tapped notify delegate`() {
        // Given
        val delegate = mock(TransactionListContract.Delegate::class.java)
        sut.delegate = delegate
        val transaction = mock(Transaction::class.java)

        // When
        sut.onTransactionTapped(transaction)

        // Then
        verify(delegate).onTransactionTapped(transaction)
    }
}
