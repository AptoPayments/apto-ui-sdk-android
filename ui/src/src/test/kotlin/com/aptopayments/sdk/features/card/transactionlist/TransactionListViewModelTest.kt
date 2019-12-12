package com.aptopayments.sdk.features.card.transactionlist

import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.transaction.MCC
import com.aptopayments.core.data.transaction.MCC.Icon.FOOD
import com.aptopayments.core.data.transaction.Transaction
import com.aptopayments.core.extension.ISO8601
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import com.nhaarman.mockitokotlin2.given
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Spy
import org.threeten.bp.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransactionListViewModelTest : AndroidTest() {

    private lateinit var sut: TransactionListViewModel

    // Collaborators
    @Spy private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()
    private val cardId = "cardId"
    private val startDate = LocalDate.of(2019, 1, 1)
    private val endDate = LocalDate.of(2019, 1, 31)
    private val mcc = MCC("plane", FOOD)
    @Mock private lateinit var mockTransaction: Transaction

    @Before
    override fun setUp() {
        super.setUp()
        sut = TransactionListViewModel(analyticsManager)
        given { mockTransaction.createdAt }.willReturn(ISO8601.parseDate("2019-01-15"))
    }

    @Test
    fun `test track is called on view loaded`() {
        sut.viewLoaded()
        assertTrue { analyticsManager.trackCalled }
        assertEquals(analyticsManager.lastEvent, Event.TransactionList)
    }
}
