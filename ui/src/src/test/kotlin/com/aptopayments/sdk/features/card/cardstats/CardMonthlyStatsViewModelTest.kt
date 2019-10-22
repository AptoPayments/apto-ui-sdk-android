package com.aptopayments.sdk.features.card.cardstats

import com.aptopayments.core.analytics.Event
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import com.aptopayments.sdk.repository.StatementRepository
import com.aptopayments.sdk.repository.StatementRepositoryImpl
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Spy
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CardMonthlyStatsViewModelTest : AndroidTest() {

    private lateinit var sut: CardMonthlyStatsViewModel

    @Spy private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()
    @Mock private lateinit var statementRepository : StatementRepository

    @Before
    override fun setUp() {
        super.setUp()
        sut = CardMonthlyStatsViewModel(analyticsManager, statementRepository)
    }

    @Test
    fun `test track is called on view loaded`() {
        sut.viewLoaded()
        assertTrue { analyticsManager.trackCalled }
        assertEquals(analyticsManager.lastEvent, Event.MonthlySpending)
    }
}
