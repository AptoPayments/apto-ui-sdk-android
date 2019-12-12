package com.aptopayments.sdk.repository

import com.aptopayments.core.data.card.CardDetails
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.utils.DateProvider
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.mockito.Spy
import org.threeten.bp.LocalDateTime

private const val EXPIRATION_THRESHOLD_SECONDS = 60L

class InMemoryLocalCardDetailsRepositoryTest : UnitTest() {

    private val dateTime = LocalDateTime.of(2019, 11, 27, 10, 41)
    private val details = CardDetails("1111222233334444", "123", "2020-11")

    @Spy
    private lateinit var dateProvider: DateProvider

    private lateinit var sut: InMemoryLocalCardDetailsRepository

    @Before
    fun setUp() {
        startKoin {
            modules(module {
                single { dateProvider }
            })
        }
        sut = InMemoryLocalCardDetailsRepository(dateProvider)
    }

    @Test
    fun `when No Data Set Then Null Getted from repository`() {
        assertNull(sut.getCardDetails())
    }

    @Test
    fun `when save cardDetails then getted details are correct`() {
        setDateTime(dateTime)

        sut.saveCardDetails(details)

        assertEquals(sut.getCardDetails(), details)
    }

    @Test
    fun `when time passes less than expected then details are not returned`() {
        setDateTime(dateTime)

        sut.saveCardDetails(details)

        setDateTime(dateTime.plusSeconds(EXPIRATION_THRESHOLD_SECONDS - 1))
        assertEquals(sut.getCardDetails(), details)
    }

    @Test
    fun `when time passes more than expected then details are not returned`() {
        setDateTime(dateTime)

        sut.saveCardDetails(details)

        setDateTime(dateTime.plusSeconds(EXPIRATION_THRESHOLD_SECONDS + 1))
        assertNull(sut.getCardDetails())
    }

    private fun setDateTime(dateTime: LocalDateTime) {
        whenever(dateProvider.localDateTime()).thenReturn(dateTime)
    }
}
