package com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.states

import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.FieldState
import com.aptopayments.sdk.utils.DateProvider
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.threeten.bp.LocalDate
import kotlin.test.assertEquals

private const val DAY = 8
private const val MONTH = 7
private const val YEAR = 2020

internal class ExpirationFieldStateResolverTest {

    private val dateProvider: DateProvider = mock()

    lateinit var sut: ExpirationFieldStateResolver

    @BeforeEach
    fun setUp() {
        whenever(dateProvider.localDate()).thenReturn(LocalDate.of(YEAR, MONTH, DAY))
        sut = ExpirationFieldStateResolver(dateProvider)
    }

    @Test
    fun `when field is null then Typing`() {
        val state = sut(null)

        assertEquals(FieldState.TYPING, state)
    }

    @Test
    fun `when field is empty then Typing`() {
        val state = sut("")

        assertEquals(FieldState.TYPING, state)
    }

    @Test
    fun `when field is not complete then Typing`() {
        val state = sut("072")

        assertEquals(FieldState.TYPING, state)
    }

    @Test
    fun `when field is correct then Correct`() {
        val state = sut("0720")

        assertEquals(FieldState.CORRECT, state)
    }

    @Test
    fun `when date is in the future then Correct`() {
        val state = sut("0820")

        assertEquals(FieldState.CORRECT, state)
    }

    @Test
    fun `when date is in the past then Error`() {
        val state = sut("0719")

        assertEquals(FieldState.ERROR, state)
    }

    @Test
    fun `when date is incorrect then Error`() {
        val state = sut("1520")

        assertEquals(FieldState.ERROR, state)
    }
}
