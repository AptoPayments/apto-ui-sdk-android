package com.aptopayments.sdk.repository

import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.utils.ManualTimer
import com.aptopayments.sdk.utils.getOrAwaitValue
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertFalse

@ExtendWith(InstantExecutorExtension::class)
class InMemoryLocalCardDetailsRepositoryTest {

    private lateinit var sut: InMemoryLocalCardDetailsRepository
    private val timer = ManualTimer()

    @BeforeEach
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
