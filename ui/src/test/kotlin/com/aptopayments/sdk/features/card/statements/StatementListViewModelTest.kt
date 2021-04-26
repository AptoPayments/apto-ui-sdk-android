package com.aptopayments.sdk.features.card.statements

import com.aptopayments.mobile.analytics.Event
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.di.useCaseModule
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin

class StatementListViewModelTest : UnitTest() {

    private lateinit var sut: StatementListViewModel

    private var analyticsManager: AnalyticsServiceContract = mock()

    @Before
    fun setUp() {
        startKoin { modules(useCaseModule) }
        sut = StatementListViewModel(analyticsManager)
    }

    @Test
    fun `test track is called on view loaded`() {
        verify(analyticsManager).track(Event.MonthlyStatementsListStart)
    }
}
