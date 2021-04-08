package com.aptopayments.sdk.features.card.orderphysical.success

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.card.orderphysical.success.OrderPhysicalCardSuccessViewModel.Action
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import kotlin.test.assertEquals

private const val CARD_ID = "id_1234"

class OrderPhysicalCardSuccessViewModelTest : UnitTest() {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    private val aptoPlatform: AptoPlatformProtocol = mock()
    private val analyticsManager: AnalyticsServiceContract = mock()

    private lateinit var sut: OrderPhysicalCardSuccessViewModel

    private fun createSut() {
        sut = OrderPhysicalCardSuccessViewModel(CARD_ID, aptoPlatform, analyticsManager)
    }

    @Test
    fun `when onDone then OrderPhysicalCardDone tracking is made`() {
        createSut()

        sut.onDone()

        verify(analyticsManager).track(Event.OrderPhysicalCardDone)
    }

    @Test
    fun `when onDone then OrderPhysicalDone action is fired`() {
        createSut()

        sut.onDone()

        assertEquals(Action.OrderPhysicalDone, sut.action.getOrAwaitValue())
    }
}
