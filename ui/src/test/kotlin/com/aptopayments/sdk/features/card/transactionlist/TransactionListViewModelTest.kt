package com.aptopayments.sdk.features.card.transactionlist

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.transaction.MCC
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.mobile.repository.transaction.TransactionListFilters
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import com.aptopayments.sdk.features.managecard.TransactionListItem
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.*
import org.junit.Test
import org.threeten.bp.LocalDate
import org.threeten.bp.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val TRANSACTION_ID_1 = "tr1"
private val transaction1 = TestDataProvider.provideTransaction(TRANSACTION_ID_1)
private val transaction2 = TestDataProvider.provideTransaction("tr2")

@Suppress("UNCHECKED_CAST")
class TransactionListViewModelTest : AndroidTest() {

    private val analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()
    private val aptoPlatform: AptoPlatformProtocol = mock()
    private val defaultMcc = MCC("plane")

    private lateinit var sut: TransactionListViewModel

    @Test
    fun `when ViewModel is created then test track is called`() {
        createSut()

        assertTrue { analyticsManager.trackCalled }
        assertEquals(analyticsManager.lastEvent, Event.TransactionList)
    }

    @Test
    fun `when ViewModel is created then transactions are fetched`() {
        createSut()

        verify(aptoPlatform).fetchCardTransactions(
            eq(TestDataProvider.provideCardId()),
            TestDataProvider.anyObject(),
            eq(true),
            eq(false),
            TestDataProvider.anyObject()
        )
    }

    @Test
    fun `when loadMore then correct previous transaction used`() {
        val captor = argumentCaptor<TransactionListFilters>()

        whenever(
            aptoPlatform.fetchCardTransactions(
                eq(TestDataProvider.provideCardId()),
                captor.capture(),
                eq(true),
                eq(false),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[4] as (Either<Failure, List<Transaction>>) -> Unit).invoke(
                listOf(transaction1).right()
            )
        }.thenAnswer { invocation ->
            (invocation.arguments[4] as (Either<Failure, List<Transaction>>) -> Unit).invoke(
                listOf(transaction2).right()
            )
        }
        createSut()

        sut.fetchMoreTransaction()

        verify(aptoPlatform, times(2)).fetchCardTransactions(
            eq(TestDataProvider.provideCardId()),
            TestDataProvider.anyObject(),
            eq(true),
            eq(false),
            TestDataProvider.anyObject()
        )
        assertEquals(null, captor.firstValue.lastTransactionId)
        assertEquals(TRANSACTION_ID_1, captor.secondValue.lastTransactionId)
    }

    @Test
    fun `when new transaction with same month then no header added`() {
        configureTwoCalls(transaction1, transaction2)
        createSut()

        sut.fetchMoreTransaction()
        val result = sut.transactionListItems.getOrAwaitValue()

        assertEquals(3, result.size)
        assertTrue { result[0] is TransactionListItem.SectionHeader }
        assertTrue { result[1] is TransactionListItem.TransactionRow }
        assertTrue { result[2] is TransactionListItem.TransactionRow }
    }

    @Test
    fun `when new transaction with different month then header added`() {
        val transaction2 = TestDataProvider.provideTransaction(createdAt = ZonedDateTime.now().plusMonths(1))
        configureTwoCalls(transaction1, transaction2)
        createSut()

        sut.fetchMoreTransaction()
        val result = sut.transactionListItems.getOrAwaitValue()

        assertEquals(4, result.size)
        assertTrue { result[0] is TransactionListItem.SectionHeader }
        assertTrue { result[1] is TransactionListItem.TransactionRow }
        assertTrue { result[2] is TransactionListItem.SectionHeader }
        assertTrue { result[3] is TransactionListItem.TransactionRow }
    }

    private fun configureTwoCalls(transaction1: Transaction, transaction2: Transaction) {
        whenever(
            aptoPlatform.fetchCardTransactions(
                eq(TestDataProvider.provideCardId()),
                TestDataProvider.anyObject(),
                eq(true),
                eq(false),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[4] as (Either<Failure, List<Transaction>>) -> Unit).invoke(
                listOf(transaction1).right()
            )
        }.thenAnswer { invocation ->
            (invocation.arguments[4] as (Either<Failure, List<Transaction>>) -> Unit).invoke(
                listOf(transaction2).right()
            )
        }
    }

    @Test
    fun `when reload then null as previous transaction used`() {
        val captor = argumentCaptor<TransactionListFilters>()

        whenever(
            aptoPlatform.fetchCardTransactions(
                eq(TestDataProvider.provideCardId()),
                captor.capture(),
                eq(true),
                eq(false),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[4] as (Either<Failure, List<Transaction>>) -> Unit).invoke(
                listOf(transaction1).right()
            )
        }
        createSut()

        sut.fetchTransaction(null)

        verify(aptoPlatform, times(2)).fetchCardTransactions(
            eq(TestDataProvider.provideCardId()),
            TestDataProvider.anyObject(),
            eq(true),
            eq(false),
            TestDataProvider.anyObject()
        )
        assertEquals(null, captor.firstValue.lastTransactionId)
        assertEquals(null, captor.secondValue.lastTransactionId)
    }

    private fun createSut(startDate: LocalDate? = null, endDate: LocalDate? = null, mcc: MCC = defaultMcc) {
        sut = TransactionListViewModel(
            TestDataProvider.provideCardId(),
            TransactionListConfig(startDate, endDate, mcc),
            analyticsManager,
            aptoPlatform
        )
    }
}
