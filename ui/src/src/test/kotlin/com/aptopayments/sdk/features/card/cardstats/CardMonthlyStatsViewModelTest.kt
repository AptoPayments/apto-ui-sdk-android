package com.aptopayments.sdk.features.card.cardstats

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.stats.MonthlySpending
import com.aptopayments.core.exception.Failure
import com.aptopayments.core.functional.Either
import com.aptopayments.core.platform.AptoPlatformProtocol
import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.di.useCaseModule
import com.aptopayments.sdk.core.extension.monthLocalized
import com.aptopayments.sdk.core.extension.monthToString
import com.aptopayments.sdk.core.extension.yearToString
import com.aptopayments.sdk.features.common.analytics.AnalyticsManagerSpy
import com.aptopayments.sdk.utils.DateProvider
import com.nhaarman.mockitokotlin2.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.koin.core.context.startKoin
import org.koin.test.KoinTest
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.Spy
import org.threeten.bp.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val CARD_ID = "1"
private const val DAY = 1
private const val MONTH = 6
private const val YEAR = 2020
private val CURRENT_DATE = LocalDate.of(YEAR, MONTH, DAY)
private const val EMPTY = ""

class CardMonthlyStatsViewModelTest : AndroidTest(), KoinTest {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    @Spy
    private var analyticsManager: AnalyticsManagerSpy = AnalyticsManagerSpy()

    @Mock
    private lateinit var aptoPlatformProtocol: AptoPlatformProtocol

    @Mock
    private lateinit var dateProvider: DateProvider

    private lateinit var sut: CardMonthlyStatsViewModel

    @Before
    override fun setUp() {
        super.setUp()
        startKoin { modules(useCaseModule) }
        whenever(dateProvider.localDate()).thenReturn(LocalDate.of(YEAR, MONTH, DAY))
        sut = CardMonthlyStatsViewModel(CARD_ID, aptoPlatformProtocol, analyticsManager, dateProvider)
    }

    @Test
    fun `test track is called on view loaded`() {
        sut.viewLoaded()

        assertTrue { analyticsManager.trackCalled }
        assertEquals(analyticsManager.lastEvent, Event.MonthlySpending)
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
        val result = MonthlySpending(true, true, listOf())
        configurePlatformCardMonthlySpending(result)

        sut.onMonthSelected(newDate)

        val prevNewDate = newDate.minusMonths(1)
        verify(aptoPlatformProtocol).cardMonthlySpending(
            eq(CARD_ID),
            eq(prevNewDate.monthToString()),
            eq(prevNewDate.yearToString()),
            any()
        )
        assertEquals(sut.addSpending.value, prevNewDate)
    }

    @Test
    fun `given MonthlySpending without previousMonth when monthSelected then no prefetch is done`() {
        val newDate = LocalDate.of(YEAR, MONTH - 1, 1)
        val result = MonthlySpending(false, true, listOf())
        configurePlatformCardMonthlySpending(result)

        sut.onMonthSelected(newDate)

        val prevNewDate = newDate.minusMonths(1)
        verify(aptoPlatformProtocol, times(0)).cardMonthlySpending(
            eq(CARD_ID),
            eq(prevNewDate.monthToString()),
            eq(prevNewDate.yearToString()),
            any()
        )
        assertEquals(sut.addSpending.value, null)
    }

    private fun configurePlatformCardMonthlySpending(result: MonthlySpending) {
        whenever(
            aptoPlatformProtocol.cardMonthlySpending(
                anyString(),
                anyString(),
                anyString(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[3] as (Either<Failure, MonthlySpending>) -> Unit).invoke(Either.Right(result))
        }
    }

    @Test
    fun `when getMonthlyStatement then calls protocol`() {
        sut.getMonthlyStatement(MONTH, YEAR) {}

        verify(aptoPlatformProtocol).fetchMonthlyStatement(eq(MONTH), eq(YEAR), any())
    }
}
