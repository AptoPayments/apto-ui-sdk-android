package com.aptopayments.sdk.features.card.statements

import com.aptopayments.core.analytics.Event
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import com.aptopayments.sdk.repository.StatementRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Spy

class StatementListViewModelTest : AndroidTest() {

    private lateinit var sut: StatementListViewModel
    @Spy
    private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()
    @Mock
    private lateinit var statementRepository: StatementRepository

    @Before
    override fun setUp() {
        super.setUp()
        sut = StatementListViewModel(analyticsManager, statementRepository)
    }

    @Test
    fun `test track is called on view loaded`() {
        sut.viewLoaded()
        assertTrue(analyticsManager.trackCalled)
        assertEquals(analyticsManager.lastEvent, Event.MonthlyStatementsListStart)
    }
}
