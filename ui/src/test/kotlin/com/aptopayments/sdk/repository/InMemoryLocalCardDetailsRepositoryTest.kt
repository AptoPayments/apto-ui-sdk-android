package com.aptopayments.sdk.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.utils.ManualTimer
import com.aptopayments.sdk.utils.getOrAwaitValue
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import kotlin.test.assertFalse

class InMemoryLocalCardDetailsRepositoryTest : UnitTest() {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    private lateinit var sut: InMemoryLocalCardDetailsRepository
    private val timer = ManualTimer()

    @Before
    fun setUp() {
        sut = InMemoryLocalCardDetailsRepository(timer)
    }

    @Test
    fun `when No Data Set Then event has false as value`() {
        assertFalse(sut.getCardDetailsEvent().getOrAwaitValue())
    }

    @Test
    fun `when showCardDetails then event has true value`() {
        sut.showCardDetails()

        assertTrue(sut.getCardDetailsEvent().getOrAwaitValue())
    }

    @Test
    fun `when showCardDetails and then hide then event has false value`() {
        sut.showCardDetails()
        sut.hideCardDetails()

        assertFalse(sut.getCardDetailsEvent().getOrAwaitValue())
    }

    @Test
    fun `when showing card details after proposed time the value turns false`() {
        sut.showCardDetails()
        timer.timeFinished()

        assertFalse(sut.getCardDetailsEvent().getOrAwaitValue())
    }
}
