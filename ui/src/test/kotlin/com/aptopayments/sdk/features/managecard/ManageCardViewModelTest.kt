@file:Suppress("UNCHECKED_CAST")

package com.aptopayments.sdk.features.managecard

import androidx.lifecycle.Observer
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.fundingsources.Balance
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.features.managecard.CardOptions
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.applicationModule
import com.aptopayments.sdk.core.di.useCaseModule
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.threeten.bp.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val CARD_ID = "CARD_ID_1"

class ManageCardViewModelTest : AndroidTest() {

    private lateinit var sut: ManageCardViewModel

    private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()

    @Mock
    private lateinit var fetchTransactionsTaskQueue: FetchTransactionsTaskQueue

    @Mock
    private lateinit var transactionsObserver: Observer<List<Transaction>?>

    @Mock
    private lateinit var aptoUiSdkProtocol: AptoUiSdkProtocol

    @Mock
    private lateinit var aptoPlatform: AptoPlatformProtocol

    @Before
    override fun setUp() {
        super.setUp()
        startKoin {
            modules(listOf(applicationModule, useCaseModule))
        }
    }

    private fun createSut(transactions: List<Transaction> = emptyList()) {
        mockFetchCard()
        mockFetchFundingSource()
        mockLoadTransactions(transactions)
        sut = ManageCardViewModel(
            CARD_ID,
            fetchTransactionsTaskQueue,
            analyticsManager,
            aptoUiSdkProtocol,
            aptoPlatform
        )
    }

    @Test
    fun `test track is called on view loaded`() {
        createSut()

        sut.viewLoaded()

        assertTrue { analyticsManager.trackCalled }
        assertEquals(analyticsManager.lastEvent, Event.ManageCard)
    }

    @Test
    fun `when no transactions then only header is present`() {
        createSut()
        sut.transactions.observeForever(transactionsObserver)

        val result = sut.transactionListItems.getOrAwaitValue()

        assertEquals(1, result.size)
        assertTrue { result[0] is TransactionListItem.HeaderView }
    }

    @Test
    fun `when one transaction then Header-Section-Transaction`() {
        val transaction = TestDataProvider.provideTransaction()
        createSut(listOf(transaction))
        sut.transactions.observeForever(transactionsObserver)

        val result = sut.transactionListItems.getOrAwaitValue()

        assertEquals(3, result.size)
        assertTrue { result[0] is TransactionListItem.HeaderView }
        assertTrue { result[1] is TransactionListItem.SectionHeader }
        assertTrue { result[2] is TransactionListItem.TransactionRow }
        assertEquals(transaction, (result[2] as TransactionListItem.TransactionRow).transaction)
    }

    @Test
    fun `section header is skipped when merging list items from an append operation of the same month and year`() {
        // Given
        mockLoadMoreTransactions(listOf(TestDataProvider.provideTransaction()))
        val oldTransaction = TestDataProvider.provideTransaction(createdAt = ZonedDateTime.now().minusNanos(1))
        createSut(listOf(oldTransaction))

        // When
        sut.getMoreTransactions()
        val result = sut.transactionListItems.getOrAwaitValue()

        // Then
        assert(result.size == 4)
        assert(result[0] is TransactionListItem.HeaderView)
        assert(result[1] is TransactionListItem.SectionHeader)
        assert(result[2] is TransactionListItem.TransactionRow)
        assert(result[3] is TransactionListItem.TransactionRow)
    }

    @Test
    fun `section header and transactions are added when merging list items from an append operation of a transaction with different month`() {
        // Given
        mockLoadMoreTransactions(
            listOf(
                TestDataProvider.provideTransaction(
                    createdAt = ZonedDateTime.now().plusMonths(1)
                )
            )
        )
        val oldTransaction = TestDataProvider.provideTransaction(createdAt = ZonedDateTime.now())
        createSut(listOf(oldTransaction))

        // When
        sut.getMoreTransactions()
        val result = sut.transactionListItems.getOrAwaitValue()

        // Then
        assert(result.size == 5)
        assert(result.first() is TransactionListItem.HeaderView)
        assert(result[1] is TransactionListItem.SectionHeader)
        assert(result[2] is TransactionListItem.TransactionRow)
        assert(result[3] is TransactionListItem.SectionHeader)
        assert(result[4] is TransactionListItem.TransactionRow)
    }

    @Test
    fun `section header and transactions are added when merging list items from an append operation of a transaction with different year`() {
        // Given
        mockLoadMoreTransactions(
            listOf(
                TestDataProvider.provideTransaction(
                    createdAt = ZonedDateTime.now().plusYears(1)
                )
            )
        )
        val oldTransaction = TestDataProvider.provideTransaction(createdAt = ZonedDateTime.now())
        createSut(listOf(oldTransaction))

        // When
        sut.getMoreTransactions()
        val result = sut.transactionListItems.getOrAwaitValue()

        // Then
        assert(result.size == 5)
        assert(result.first() is TransactionListItem.HeaderView)
        assert(result[1] is TransactionListItem.SectionHeader)
        assert(result[2] is TransactionListItem.TransactionRow)
        assert(result[3] is TransactionListItem.SectionHeader)
        assert(result[4] is TransactionListItem.TransactionRow)
    }

    @Test
    fun `section header and transactions are added when merging list items from pull to refresh and there are no current transactions`() {
        // Given
        val newTransaction = TestDataProvider.provideTransaction(createdAt = ZonedDateTime.now())
        mockLoadTransactions(listOf(newTransaction), forceApiCall = true, clearCachedValue = true)
        createSut(listOf())

        // When
        sut.refreshTransactions()
        val result = sut.transactionListItems.getOrAwaitValue()

        // Then
        assert(result.size == 3)
        assert(result.first() is TransactionListItem.HeaderView)
        assert(result[1] is TransactionListItem.SectionHeader)
        assert(result[2] is TransactionListItem.TransactionRow)
    }

    @Test
    fun `section header and transactions are added when merging list items from background refresh and there is one transaction`() {
        // Given
        val newTransaction = TestDataProvider.provideTransaction(createdAt = ZonedDateTime.now().plusMonths(1))
        val oldTransaction = TestDataProvider.provideTransaction(createdAt = ZonedDateTime.now())
        mockBackgroundTransactions(listOf(newTransaction, oldTransaction))
        createSut(listOf(oldTransaction))

        // When
        val result = sut.transactionListItems.getOrAwaitValue()

        // Then
        assertEquals(5, result.size)
        assert(result[0] is TransactionListItem.HeaderView)
        assert(result[1] is TransactionListItem.SectionHeader)
        assert(result[2] is TransactionListItem.TransactionRow)
        assert(result[3] is TransactionListItem.SectionHeader)
        assert(result[4] is TransactionListItem.TransactionRow)
        assertEquals(newTransaction, (result[2] as TransactionListItem.TransactionRow).transaction)
    }

    @Test
    fun `whenever embedded then X is shown and Back is allowed`() {
        whenever(aptoUiSdkProtocol.cardOptions).thenReturn(CardOptions(openingMode = CardOptions.OpeningMode.EMBEDDED))
        createSut()

        assertTrue { sut.canBackPress }
        assertTrue { sut.showXOnToolbar }
    }

    @Test
    fun `whenever embedded then X is not shown and Back is not allowed`() {
        whenever(aptoUiSdkProtocol.cardOptions).thenReturn(CardOptions(openingMode = CardOptions.OpeningMode.STANDALONE))
        createSut()

        assertFalse(sut.canBackPress)
        assertFalse(sut.showXOnToolbar)
    }

    private fun mockFetchCard() {
        whenever(
            aptoPlatform.fetchCard(
                eq(CARD_ID),
                any(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Card>) -> Unit).invoke(
                TestDataProvider.provideCard(accountID = CARD_ID).right()
            )
        }
    }

    private fun mockFetchFundingSource() {
        whenever(
            aptoPlatform.fetchCardFundingSource(
                eq(CARD_ID),
                any(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Balance>) -> Unit).invoke(Balance().right())
        }
    }

    private fun mockLoadTransactions(
        transactions: List<Transaction>,
        forceApiCall: Boolean = false,
        clearCachedValue: Boolean = false
    ) {
        whenever(
            fetchTransactionsTaskQueue.loadTransactions(
                eq(CARD_ID),
                anyInt(),
                eq(forceApiCall),
                eq(clearCachedValue),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[4] as (Either<Failure, List<Transaction>>) -> Unit).invoke(transactions.right())
        }
    }

    private fun mockLoadMoreTransactions(transactions: List<Transaction>) {
        whenever(
            fetchTransactionsTaskQueue.loadMoreTransactions(
                eq(CARD_ID),
                anyString(),
                anyInt(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[3] as (Either<Failure, List<Transaction>>) -> Unit).invoke(transactions.right())
        }
    }

    private fun mockBackgroundTransactions(transactions: List<Transaction>) {
        whenever(
            fetchTransactionsTaskQueue.backgroundRefresh(
                eq(CARD_ID),
                anyInt(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, List<Transaction>>) -> Unit).invoke(transactions.right())
        }
    }
}
