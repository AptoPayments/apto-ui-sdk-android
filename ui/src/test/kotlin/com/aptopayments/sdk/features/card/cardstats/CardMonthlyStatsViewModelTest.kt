package com.aptopayments.sdk.features.card.cardstats

import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.stats.MonthlySpending
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.useCaseModule
import com.aptopayments.sdk.core.extension.monthLocalized
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.DateProvider
import org.mockito.kotlin.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.context.startKoin
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.threeten.bp.LocalDate
import kotlin.test.assertEquals

private const val CARD_ID = "1"
private const val DAY = 1
private const val MONTH = 6
private const val YEAR = 2020
private val CURRENT_DATE = LocalDate.of(YEAR, MONTH, DAY)
private const val EMPTY = ""

@ExtendWith(InstantExecutorExtension::class)
class CardMonthlyStatsViewModelTest : UnitTest() {

    private val analyticsManager: AnalyticsServiceContract = mock()
    private val aptoPlatformProtocol: AptoPlatformProtocol = mock()
    private val dateProvider: DateProvider = mock()

    private lateinit var sut: CardMonthlyStatsViewModel

    @BeforeEach
    fun setUp() {
        startKoin { modules(useCaseModule) }
        whenever(dateProvider.localDate()).thenReturn(LocalDate.of(YEAR, MONTH, DAY))
        sut = CardMonthlyStatsViewModel(CARD_ID, aptoPlatformProtocol, analyticsManager, dateProvider)
    }

    @Test
    fun `test track is called on init`() {
        verify(analyticsManager).track(Event.MonthlySpending)
    }

    @Test
    fun `when created then currentMonthName is correct`() {
        assertEquals(sut.previousMonthName.value, EMPTY)
        assertEquals(sut.currentMonthName.value, LocalDate.of(YEAR, MONTH, 1).monthLocalized())
        assertEquals(sut.nextMonthName.value, EMPTY)
    }

    @Test
    fun `when monthSelected with CURRENT month then names are modified before callback executed`() {
        sut.onMonthSelected(CURRENT_DATE)

        assertEquals(sut.previousMonthName.value, EMPTY)
        assertEquals(sut.currentMonthName.value, LocalDate.of(YEAR, MONTH, 1).monthLocalized())
        assertEquals(sut.nextMonthName.value, EMPTY)
    }

    @Test
    fun `when monthSelected with NEXT month then names are modified before callback executed`() {
        val newDate = LocalDate.of(YEAR, MONTH + 1, 1)

        sut.onMonthSelected(newDate)

        assertEquals(sut.previousMonthName.value, CURRENT_DATE.monthLocalized())
        assertEquals(sut.currentMonthName.value, newDate.monthLocalized())
        assertEquals(sut.nextMonthName.value, EMPTY)
    }

    @Test
    fun `when monthSelected with PREVIOUS month then names are modified before callback executed`() {
        val newDate = LocalDate.of(YEAR, MONTH - 1, 1)

        sut.onMonthSelected(newDate)

        assertEquals(sut.previousMonthName.value, EMPTY)
        assertEquals(sut.currentMonthName.value, newDate.monthLocalized())
        assertEquals(sut.nextMonthName.value, CURRENT_DATE.monthLocalized())
    }

    @Test
    fun `when monthSelected with NEXT month then names are modified AFTER callback executed`() {
        val newDate = LocalDate.of(YEAR, MONTH + 1, 1)
        val prevExists = true
        val nextExists = true

        val result = MonthlySpending(prevExists, nextExists, listOf())
        configurePlatformCardMonthlySpending(result)
        sut.onMonthSelected(newDate)

        assertEquals(sut.previousMonthName.value, CURRENT_DATE.monthLocalized())
        assertEquals(sut.currentMonthName.value, newDate.monthLocalized())
        assertEquals(sut.nextMonthName.value, newDate.plusMonths(1).monthLocalized())
    }

    @Test
    fun `when monthSelected with PREVIOUS month then names are modified AFTER callback executed`() {
        val newDate = LocalDate.of(YEAR, MONTH - 1, 1)
        val prevExists = true
        val nextExists = true

        val result = MonthlySpending(prevExists, nextExists, listOf())
        configurePlatformCardMonthlySpending(result)
        sut.onMonthSelected(newDate)

        assertEquals(sut.previousMonthName.value, newDate.minusMonths(1).monthLocalized())
        assertEquals(sut.currentMonthName.value, newDate.monthLocalized())
        assertEquals(sut.nextMonthName.value, CURRENT_DATE.monthLocalized())
    }

    @Test
    fun `given MonthlySpending with previousMonth when monthSelected then prefetch and emit previous month`() {
        val newDate = LocalDate.of(YEAR, MONTH - 1, 1)
        val result = MonthlySpending(prevSpendingExists = true, nextSpendingExists = true, spending = listOf())
        configurePlatformCardMonthlySpending(result)

        sut.onMonthSelected(newDate)

        val prevNewDate = newDate.minusMonths(1)
        verify(aptoPlatformProtocol).cardMonthlySpending(
            eq(CARD_ID),
            eq(MONTH - 1),
            eq(YEAR),
            any()
        )
        assertEquals(sut.addSpending.value, prevNewDate)
    }

    @Test
    fun `given MonthlySpending without previousMonth when monthSelected then no prefetch is done`() {
        val newDate = LocalDate.of(YEAR, MONTH, 1)
        val result = MonthlySpending(prevSpendingExists = false, nextSpendingExists = true, spending = listOf())
        configurePlatformCardMonthlySpending(result)

        sut.onMonthSelected(newDate)

        val prevNewDate = newDate.minusMonths(1)
        verify(aptoPlatformProtocol, times(0)).cardMonthlySpending(
            eq(CARD_ID),
            eq(prevNewDate.monthValue),
            eq(prevNewDate.year),
            any()
        )
        assertEquals(sut.addSpending.value, null)
    }

    @Suppress("UNCHECKED_CAST")
    private fun configurePlatformCardMonthlySpending(result: MonthlySpending) {
        whenever(
            aptoPlatformProtocol.cardMonthlySpending(
                anyString(),
                anyInt(),
                anyInt(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[3] as (Either<Failure, MonthlySpending>) -> Unit).invoke(Either.Right(result))
        }
    }
}
