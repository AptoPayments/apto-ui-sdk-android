package com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.states

import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.FieldState
import com.aptopayments.sdk.utils.DateProvider
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.threeten.bp.LocalDate
import kotlin.test.assertEquals

private const val DAY = 8
private const val MONTH = 7
private const val YEAR = 2020

internal class ExpirationFieldStateResolverTest : UnitTest() {

    @Mock
    private lateinit var dateProvider: DateProvider

    lateinit var sut: ExpirationFieldStateResolver

    @Before
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
