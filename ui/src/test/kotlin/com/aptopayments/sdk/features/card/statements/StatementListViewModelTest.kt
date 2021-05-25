package com.aptopayments.sdk.features.card.statements

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule

class StatementListViewModelTest : UnitTest() {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    private lateinit var sut: StatementListViewModel

    private var analyticsManager: AnalyticsServiceContract = mock()
    private val aptoPlatform: AptoPlatformProtocol = mock()

    @Before
    fun setUp() {
        sut = StatementListViewModel(analyticsManager, aptoPlatform)
    }

    @Test
    fun `test track is called on view loaded`() {
        verify(analyticsManager).track(Event.MonthlyStatementsListStart)
    }
}
