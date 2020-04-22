package com.aptopayments.sdk.features.managecard

import androidx.lifecycle.Observer
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.transaction.Transaction
import com.aptopayments.core.repository.transaction.FetchTransactionsTaskQueue
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.applicationModule
import com.aptopayments.sdk.core.di.useCaseModule
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Spy
import org.threeten.bp.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManageCardViewModelTest : AndroidTest() {

    private lateinit var sut: ManageCardViewModel

    @Spy private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()

    @Mock private lateinit var fetchTransactionsTaskQueue: FetchTransactionsTaskQueue
    @Mock private lateinit var mockCard: Card
    @Mock private lateinit var mockTransaction: Transaction
    @Mock private lateinit var transactionsObserver: Observer<List<Transaction>?>
    @Mock private lateinit var transactionListItemsObserver: Observer<List<TransactionListItem>?>
    @Mock private lateinit var aptoUiSdkProtocol: AptoUiSdkProtocol

    private val EXPECTED_WAITLIST_VALUE = true

    @Before
    override fun setUp() {
        super.setUp()
        startKoin {
            modules( listOf(applicationModule,useCaseModule) )
        }
        sut = ManageCardViewModel(TestDataProvider.provideCardId(), fetchTransactionsTaskQueue, analyticsManager, aptoUiSdkProtocol)
        `when`(mockCard.isWaitlisted).thenReturn(EXPECTED_WAITLIST_VALUE)
        `when`(mockTransaction.createdAt).thenReturn(ZonedDateTime.now())
        `when`(mockTransaction.transactionId).thenReturn("")
    }

    @Test
    fun `build items does not add section header when skipFirstHeader is true`() {

        // Given
        val transactions: MutableList<Transaction> = mutableListOf()
        transactions.add(mockTransaction)

        // When
        val result = sut.buildItems(transactions, skipFirstHeader = true)

        // Then
        assert(result.size == 1)
        assert(result.first().itemType() == TransactionListItem.TRANSACTION_ROW_VIEW_TYPE)
        assert((result.first() as TransactionListItem.TransactionRow).transaction == mockTransaction)
    }

    @Test
    fun `build items does adds section header when skipFirstHeader is false`() {

        // Given
        val transactions: MutableList<Transaction> = mutableListOf()
        transactions.add(mockTransaction)

        // When
        val result = sut.buildItems(transactions, skipFirstHeader = false)

        // Then
        assert(result.size == 2)
        assert(result.first().itemType() == TransactionListItem.SECTION_HEADER_VIEW_TYPE)
        assert(result[1].itemType() == TransactionListItem.TRANSACTION_ROW_VIEW_TYPE)
        assert((result[1] as TransactionListItem.TransactionRow).transaction == mockTransaction)
    }

    @Test
    fun `test track is called on view loaded`() {
        sut.viewLoaded()
        assertTrue { analyticsManager.trackCalled }
        assertEquals(analyticsManager.lastEvent, Event.ManageCard)
    }

    @Test
    fun `section header is skipped when merging list items from an append operation of the same month and year`() {

        // Given
        sut.transactionListItems.observeForever(transactionListItemsObserver)
        val newTransactions: MutableList<Transaction> = mutableListOf()
        newTransactions.add(mockTransaction)

        val currentTransactions: MutableList<TransactionListItem> = mutableListOf()
        currentTransactions.add(TransactionListItem.HeaderView)
        currentTransactions.add(TransactionListItem.SectionHeader(""))
        currentTransactions.add(TransactionListItem.TransactionRow(mockTransaction))

        // When
        sut.mergeListItems(newTransactions = newTransactions, currentTransactionItems = currentTransactions, append = true)

        // Then
        assert(sut.transactionListItems.value?.size == 4)
        assert(sut.transactionListItems.value?.first() is TransactionListItem.HeaderView)
        assert(sut.transactionListItems.value!![1] is TransactionListItem.SectionHeader)
        assert(sut.transactionListItems.value!![2] is TransactionListItem.TransactionRow)
        assert(sut.transactionListItems.value!![3] is TransactionListItem.TransactionRow)
    }

    @Test
    fun `section header and transactions are added when merging list items from an append operation of a transaction with different month`() {

        // Given
        sut.transactionListItems.observeForever(transactionListItemsObserver)
        val newTransactions: MutableList<Transaction> = mutableListOf()
        val newMockTransaction: Transaction = mock(Transaction::class.java)
        `when`(newMockTransaction.createdAt).thenReturn(ZonedDateTime.now().plusMonths(1))
        newTransactions.add(newMockTransaction)

        val currentTransactions: MutableList<TransactionListItem> = mutableListOf()
        currentTransactions.add(TransactionListItem.HeaderView)
        currentTransactions.add(TransactionListItem.SectionHeader(""))
        currentTransactions.add(TransactionListItem.TransactionRow(mockTransaction))

        // When
        sut.mergeListItems(newTransactions = newTransactions, currentTransactionItems = currentTransactions, append = true)

        // Then
        assert(sut.transactionListItems.value?.size == 5)
        assert(sut.transactionListItems.value?.first() is TransactionListItem.HeaderView)
        assert(sut.transactionListItems.value!![1] is TransactionListItem.SectionHeader)
        assert(sut.transactionListItems.value!![2] is TransactionListItem.TransactionRow)
        assert(sut.transactionListItems.value!![3] is TransactionListItem.SectionHeader)
        assert(sut.transactionListItems.value!![4] is TransactionListItem.TransactionRow)
    }

    @Test
    fun `section header and transactions are added when merging list items from an append operation of a transaction with different year`() {

        // Given
        sut.transactionListItems.observeForever(transactionListItemsObserver)
        val newTransactions: MutableList<Transaction> = mutableListOf()
        val newMockTransaction: Transaction = mock(Transaction::class.java)
        val date = ZonedDateTime.now().plusYears(1)
        `when`(newMockTransaction.createdAt).thenReturn(date)
        newTransactions.add(newMockTransaction)

        val currentTransactions: MutableList<TransactionListItem> = mutableListOf()
        currentTransactions.add(TransactionListItem.HeaderView)
        currentTransactions.add(TransactionListItem.SectionHeader(""))
        currentTransactions.add(TransactionListItem.TransactionRow(mockTransaction))

        // When
        sut.mergeListItems(newTransactions = newTransactions, currentTransactionItems = currentTransactions, append = true)

        // Then
        assert(sut.transactionListItems.value?.size == 5)
        assert(sut.transactionListItems.value?.first() is TransactionListItem.HeaderView)
        assert(sut.transactionListItems.value!![1] is TransactionListItem.SectionHeader)
        assert(sut.transactionListItems.value!![2] is TransactionListItem.TransactionRow)
        assert(sut.transactionListItems.value!![3] is TransactionListItem.SectionHeader)
        assert(sut.transactionListItems.value!![4] is TransactionListItem.TransactionRow)
    }

    @Test
    fun `section header and transactions are added when merging list items from pull to refresh and there are no current transactions`() {

        // Given
        sut.transactionListItems.observeForever(transactionListItemsObserver)
        val newTransactions: MutableList<Transaction> = mutableListOf()
        newTransactions.add(mockTransaction)

        val currentTransactions: MutableList<TransactionListItem> = mutableListOf()
        currentTransactions.add(TransactionListItem.HeaderView)

        // When
        sut.mergeListItems(newTransactions = newTransactions, currentTransactionItems = currentTransactions, append = false)

        // Then
        assert(sut.transactionListItems.value?.size == 3)
        assert(sut.transactionListItems.value?.first() is TransactionListItem.HeaderView)
        assert(sut.transactionListItems.value!![1] is TransactionListItem.SectionHeader)
        assert(sut.transactionListItems.value!![2] is TransactionListItem.TransactionRow)
    }

    @Test
    fun `section header and transactions are added when merging list items from background refresh and there is one transaction`() {

        // Given
        sut.transactionListItems.observeForever(transactionListItemsObserver)
        val newTransactions: MutableList<Transaction> = mutableListOf()
        val newMockTransaction: Transaction = mock(Transaction::class.java)
        val date = ZonedDateTime.now().plusMonths(1)
        `when`(newMockTransaction.createdAt).thenReturn(date)
        newTransactions.add(newMockTransaction)
        newTransactions.add(mockTransaction)

        val currentTransactions: MutableList<TransactionListItem> = mutableListOf()
        currentTransactions.add(TransactionListItem.HeaderView)
        currentTransactions.add(TransactionListItem.SectionHeader(""))
        currentTransactions.add(TransactionListItem.TransactionRow(mockTransaction))

        // When
        sut.mergeListItems(newTransactions = newTransactions, currentTransactionItems = currentTransactions, append = false)

        // Then
        assert(sut.transactionListItems.value?.size == 5)
        assert(sut.transactionListItems.value?.first() is TransactionListItem.HeaderView)
        assert(sut.transactionListItems.value!![1] is TransactionListItem.SectionHeader)
        assert(sut.transactionListItems.value!![2] is TransactionListItem.TransactionRow)
        assert(sut.transactionListItems.value!![3] is TransactionListItem.SectionHeader)
        assert(sut.transactionListItems.value!![4] is TransactionListItem.TransactionRow)
    }

    @Test
    fun `no update when update transactions is called with no transactions`() {

        // Given
        sut.transactions.observeForever(transactionsObserver)
        sut.transactions.value = emptyList()
        val newTransactions: MutableList<Transaction> = mutableListOf()

        // When
        val result = sut.updateTransactions(transactionList = newTransactions, append = false)

        // Then
        assert(result == newTransactions)
        assert(sut.transactions.value?.isEmpty()!!)
    }

    @Test
    fun `transactions are appended when update transactions is called with transactions`() {

        // Given
        sut.transactions.observeForever(transactionsObserver)
        sut.transactions.value = emptyList()
        val newTransactions: MutableList<Transaction> = mutableListOf()
        newTransactions.add(mockTransaction)

        // When
        val result = sut.updateTransactions(transactionList = newTransactions, append = true)

        // Then
        assert(result == newTransactions)
        assert(sut.transactions.value?.first() == mockTransaction)
    }

    @Test
    fun `transactions are added when update transactions is called after pull to refresh`() {

        // Given
        sut.transactions.observeForever(transactionsObserver)
        sut.transactions.value = emptyList()
        val newTransactions: MutableList<Transaction> = mutableListOf()
        newTransactions.add(mockTransaction)

        // When
        val result = sut.updateTransactions(transactionList = newTransactions, append = false)

        // Then
        assert(result == newTransactions)
        assert(sut.transactions.value?.first() == mockTransaction)
    }

    @Test
    fun `transactions are merged when update transactions is called after background refresh`() {

        // Given
        sut.transactions.observeForever(transactionsObserver)
        val currentTransactions: MutableList<Transaction> = mutableListOf()
        val currentMockTransaction: Transaction = mock(Transaction::class.java)
        val date = ZonedDateTime.now().plusMonths(1)
        `when`(currentMockTransaction.createdAt).thenReturn(date)
        currentTransactions.add(currentMockTransaction)
        currentTransactions.add(mockTransaction)
        sut.transactions.value = currentTransactions
        val newTransactions: MutableList<Transaction> = mutableListOf()
        newTransactions.add(mockTransaction)

        // When
        val result = sut.updateTransactions(transactionList = newTransactions, append = false)

        // Then
        assert(result.size == 2)
        assert(sut.transactions.value?.first() == currentMockTransaction)
        assert(sut.transactions.value!![1] == mockTransaction)
    }

    @Test
    fun `transactions are replaced when update transactions is called with new transactions and previous ones are not present after background refresh`() {

        // Given
        sut.transactions.observeForever(transactionsObserver)
        val currentTransactions: MutableList<Transaction> = mutableListOf()
        currentTransactions.add(mockTransaction)
        val newMockTransaction: Transaction = mock(Transaction::class.java)
        val date = ZonedDateTime.now().plusMonths(1)
        `when`(newMockTransaction.createdAt).thenReturn(date)
        sut.transactions.value = currentTransactions
        val newTransactions: MutableList<Transaction> = mutableListOf()
        newTransactions.add(newMockTransaction)

        // When
        val result = sut.updateTransactions(transactionList = newTransactions, append = false)

        // Then
        assert(result.size == 1)
        assert(sut.transactions.value?.first() == newMockTransaction)
    }
}
