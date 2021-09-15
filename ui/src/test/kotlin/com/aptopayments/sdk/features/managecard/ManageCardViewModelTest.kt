@file:Suppress("UNCHECKED_CAST")

package com.aptopayments.sdk.features.managecard

import androidx.lifecycle.Observer
import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.Features
import com.aptopayments.mobile.data.card.InAppProvisioningFeature
import com.aptopayments.mobile.data.fundingsources.Balance
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.features.managecard.CardOptions
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.applicationModule
import com.aptopayments.sdk.core.di.useCaseModule
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.repository.IAPHelper
import com.aptopayments.sdk.repository.ProvisioningState
import com.aptopayments.sdk.utils.getOrAwaitValue
import kotlinx.coroutines.flow.MutableStateFlow
import org.mockito.kotlin.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.threeten.bp.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

private const val CARD_ID = "CARD_ID_1"

@ExtendWith(InstantExecutorExtension::class)
class ManageCardViewModelTest : UnitTest() {

    private lateinit var sut: ManageCardViewModel

    private var analyticsManager: AnalyticsServiceContract = mock()
    private val fetchTransactionsTaskQueue: FetchTransactionsTaskQueue = mock()
    private val transactionsObserver: Observer<List<Transaction>?> = mock()
    private val aptoUiSdkProtocol: AptoUiSdkProtocol = mock()
    private val aptoPlatform: AptoPlatformProtocol = mock()
    private val iapHelper: IAPHelper = mock()

    @BeforeEach
    fun setUp() {
        startKoin {
            modules(
                listOf(
                    applicationModule, useCaseModule,
                    module {
                        factory(override = true) { iapHelper }
                    }
                )
            )
        }
    }

    private fun createSut(
        transactions: List<Transaction> = emptyList(),
        card: Card = TestDataProvider.provideCard(accountID = CARD_ID),
        cardOptions: CardOptions = CardOptions(),
        iapState: ProvisioningState = ProvisioningState.CanNotBeAdded()
    ) {
        mockFetchCard(card)
        mockFetchFundingSource()
        mockLoadTransactions(transactions)
        givenCardOptions(cardOptions)
        givenIapState(iapState)
        sut = ManageCardViewModel(
            CARD_ID,
            fetchTransactionsTaskQueue,
            analyticsManager,
            aptoUiSdkProtocol,
            aptoPlatform
        )
    }

    @Test
    fun `test track is called on init`() {
        createSut()

        verify(analyticsManager).track(Event.ManageCard)
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
        createSut(cardOptions = CardOptions(openingMode = CardOptions.OpeningMode.EMBEDDED))

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

    @Test
    fun `given no api call is made when load then menu options are in default`() {
        createSut()

        val menu = sut.menuState.getOrAwaitValue()

        assertFalse(menu.showPhysicalCardActivationMessage)
        assertTrue(menu.showAccountSettings)
        assertFalse(menu.showStats)
    }

    @Test
    fun `given no api call is made and showStatButtons when load then ShowStats is true`() {
        createSut(cardOptions = CardOptions(showStatsButton = true, showAccountSettingsButton = false))

        val menu = sut.menuState.getOrAwaitValue()

        assertFalse(menu.showPhysicalCardActivationMessage)
        assertFalse(menu.showAccountSettings)
        assertTrue(menu.showStats)
    }

    @Test
    fun `given no api call is made and showAccountSettings when load then showAccountSettings is true`() {
        createSut(cardOptions = CardOptions(showAccountSettingsButton = true, showStatsButton = false))

        val menu = sut.menuState.getOrAwaitValue()

        assertFalse(menu.showPhysicalCardActivationMessage)
        assertTrue(menu.showAccountSettings)
        assertFalse(menu.showStats)
    }

    @Test
    fun `given api call is configured when load then showAccountSettings is true`() {
        val card = TestDataProvider.provideCard(accountID = CARD_ID, orderedStatus = Card.OrderedStatus.ORDERED)
        createSut(card = card)

        val menu = sut.menuState.getOrAwaitValue()

        assertTrue(menu.showPhysicalCardActivationMessage)
    }

    @Test
    internal fun `given No transactions and iap not working then empty state is shown`() {
        createSut(transactions = emptyList(), iapState = ProvisioningState.CanNotBeAdded())

        val emptyState = sut.emptyState.getOrAwaitValue()

        assertTrue(emptyState.showContainer)
        assertTrue(emptyState.showNoTransactions)
        assertFalse(emptyState.showAddToGPay)
    }

    @Test
    internal fun `given No transactions and iap CanBeAdded but feature is off then empty state is shown`() {
        whenever(iapHelper.satisfyHardwareRequisites()).thenReturn(true)
        val cardFeatures = Features(inAppProvisioning = InAppProvisioningFeature(isEnabled = false))
        val card = TestDataProvider.provideCard(features = cardFeatures)
        createSut(transactions = emptyList(), card = card, iapState = ProvisioningState.CanBeAdded())

        val emptyState = sut.emptyState.getOrAwaitValue()

        assertTrue(emptyState.showContainer)
        assertTrue(emptyState.showNoTransactions)
        assertFalse(emptyState.showAddToGPay)
    }

    @Test
    internal fun `given No transactions and iap CanBeAdded then addToGpay is shown`() {
        whenever(iapHelper.satisfyHardwareRequisites()).thenReturn(true)
        val cardFeatures = Features(inAppProvisioning = InAppProvisioningFeature(isEnabled = true))
        val card = TestDataProvider.provideCard(features = cardFeatures)
        createSut(transactions = emptyList(), card = card, iapState = ProvisioningState.CanBeAdded())

        val emptyState = sut.emptyState.getOrAwaitValue()

        assertTrue(emptyState.showContainer)
        assertFalse(emptyState.showNoTransactions)
        assertTrue(emptyState.showAddToGPay)
    }

    @Test
    internal fun `given transactions and iap CanBeAdded then empty state is not shown`() {
        whenever(iapHelper.satisfyHardwareRequisites()).thenReturn(true)
        createSut(
            transactions = listOf(TestDataProvider.provideTransaction("id1")),
            iapState = ProvisioningState.CanBeAdded()
        )

        val emptyState = sut.emptyState.getOrAwaitValue()

        assertFalse(emptyState.showContainer)
        assertFalse(emptyState.showNoTransactions)
    }

    private fun mockFetchCard(card: Card) {
        whenever(
            aptoPlatform.fetchCard(
                eq(CARD_ID),
                any(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[2] as (Either<Failure, Card>) -> Unit).invoke(
                card.right()
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

    private fun givenCardOptions(cardOptions: CardOptions) {
        whenever(aptoUiSdkProtocol.cardOptions).thenReturn(cardOptions)
    }

    private fun givenIapState(iapState: ProvisioningState) {
        whenever(iapHelper.state).thenReturn(MutableStateFlow(iapState))
    }
}
