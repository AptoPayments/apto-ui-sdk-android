package com.aptopayments.sdk.features.card.cardstats

import com.aptopayments.core.analytics.Event
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.di.useCaseModule
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.test.KoinTest
import org.mockito.Spy
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CardMonthlyStatsViewModelTest : AndroidTest(), KoinTest {

    private lateinit var sut: CardMonthlyStatsViewModel

    @Spy
    private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()

    @Before
    override fun setUp() {
        super.setUp()
        startKoin { modules(useCaseModule) }
        sut = CardMonthlyStatsViewModel(analyticsManager)
    }

    @Test
    fun `test track is called on view loaded`() {
        sut.viewLoaded()
        assertTrue { analyticsManager.trackCalled }
        assertEquals(analyticsManager.lastEvent, Event.MonthlySpending)
    }
}
