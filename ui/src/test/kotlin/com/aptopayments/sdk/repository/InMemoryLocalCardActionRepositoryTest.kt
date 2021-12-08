package com.aptopayments.sdk.repository

import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.utils.ManualTimer
import com.aptopayments.sdk.utils.getOrAwaitValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals

@ExtendWith(InstantExecutorExtension::class)
class InMemoryLocalCardActionRepositoryTest {

    private lateinit var sut: InMemoryLocalCardActionRepository
    private val timer = ManualTimer()

    @BeforeEach
    fun setUp() {
        sut = InMemoryLocalCardActionRepository(timer)
    }

    @Test
    fun `when No Data Set Then event is IDLE`() {
        assertEquals(CardAction.IDLE, sut.event.getOrAwaitValue())
    }

    @Test
    fun `when showCardDetails then event has SHOW_DETAILS value`() {
        sut.showCardDetails()

        assertEquals(CardAction.SHOW_DETAILS, sut.event.getOrAwaitValue())
    }

    @Test
    fun `when showCardDetails and then hide then event has HIDE_DETAILS value`() {
        sut.showCardDetails()
        sut.hideCardDetails()

        assertEquals(CardAction.HIDE_DETAILS, sut.event.getOrAwaitValue())
    }

    @Test
    fun `when showing card details after proposed time the value turns HIDE_DETAILS`() {
        sut.showCardDetails()
        timer.timeFinished()

        assertEquals(CardAction.HIDE_DETAILS, sut.event.getOrAwaitValue())
    }

    @Test
    fun `when setPin then event is SET_PIN`() {
        sut.setPin()

        assertEquals(CardAction.SET_PIN, sut.event.getOrAwaitValue())
    }

    @Test
    fun `when setting pin and timer finished, event is still SetPin`() {
        sut.showCardDetails()
        sut.setPin()
        timer.timeFinished()

        assertEquals(CardAction.SET_PIN, sut.event.getOrAwaitValue())
    }
}
