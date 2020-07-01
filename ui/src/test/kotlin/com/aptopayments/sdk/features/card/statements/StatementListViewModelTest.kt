package com.aptopayments.sdk.features.card.statements

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.di.useCaseModule
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.test.KoinTest
import org.mockito.Spy

class StatementListViewModelTest : AndroidTest(), KoinTest {

    private lateinit var sut: StatementListViewModel

    @Spy
    private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()

    @Before
    override fun setUp() {
        super.setUp()
        startKoin { modules(useCaseModule) }
        sut = StatementListViewModel(analyticsManager)
    }

    @Test
    fun `test track is called on view loaded`() {
        sut.viewLoaded()
        assertTrue(analyticsManager.trackCalled)
        assertEquals(analyticsManager.lastEvent, Event.MonthlyStatementsListStart)
    }
}
