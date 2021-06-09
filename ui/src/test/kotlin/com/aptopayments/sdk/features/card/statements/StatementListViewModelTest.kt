package com.aptopayments.sdk.features.card.statements

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class)
class StatementListViewModelTest : UnitTest() {

    private lateinit var sut: StatementListViewModel

    private var analyticsManager: AnalyticsServiceContract = mock()
    private val aptoPlatform: AptoPlatformProtocol = mock()

    @BeforeEach
    fun setUp() {
        sut = StatementListViewModel(analyticsManager, aptoPlatform)
    }

    @Test
    fun `test track is called on view loaded`() {
        verify(analyticsManager).track(Event.MonthlyStatementsListStart)
    }
}
