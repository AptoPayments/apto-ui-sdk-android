package com.aptopayments.sdk.features.card.cardstats.chart

import com.aptopayments.mobile.data.statements.MonthlyStatementPeriod
import com.aptopayments.mobile.data.stats.CategorySpending
import com.aptopayments.mobile.data.stats.MonthlySpending
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.features.managecard.CardOptions
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.InstantExecutorExtension
import com.aptopayments.sdk.UnitTest
import com.aptopayments.sdk.core.data.TestDataProvider
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.utils.getOrAwaitValue
import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.core.context.startKoin
import org.koin.dsl.module
import org.mockito.ArgumentMatchers
import org.threeten.bp.LocalDate

private const val CARD_ID = "1"
private const val DAY = 1
private const val MONTH = 6
private const val YEAR = 2020
private val CURRENT_DATE = LocalDate.of(YEAR, MONTH, DAY)

@Suppress("UNCHECKED_CAST")
@ExtendWith(InstantExecutorExtension::class)
class CardTransactionsChartViewModelTest : UnitTest() {

    private val sorter: CategorySpendingSorter = mock()
    private val aptoPlatform: AptoPlatformProtocol = mock()
    private lateinit var sut: CardTransactionsChartViewModel

    @BeforeEach
    fun setUp() {
        startKoin { modules(module { factory { sorter } }) }
    }

    private fun createSut() {
        sut = CardTransactionsChartViewModel(CARD_ID, CURRENT_DATE, aptoPlatform)
    }

    @Test
    fun `when initialized then cardMonthlySpending call is made`() {
        createSut()

        verify(aptoPlatform).cardMonthlySpending(
            eq(CARD_ID),
            eq(CURRENT_DATE.monthValue),
            eq(CURRENT_DATE.year),
            any()
        )
    }

    @Test
    fun `when Api spendingList is empty then viewModel categorySpending is empty`() {
        val list = listOf<CategorySpending>()
        configurePlatformCardMonthlySpending(MonthlySpending(false, false, list))

        createSut()

        assertEquals(sut.categorySpending.getOrAwaitValue().size, list.size)
    }

    @Test
    fun `when Api spendingList has 2 values then viewModel categorySpending has 2 values`() {
        val list = TestDataProvider.provideCategorySpendingList()
        whenever(sorter.sortByName(list)).thenReturn(list)

        configurePlatformCardMonthlySpending(MonthlySpending(false, false, list))

        createSut()

        val value = sut.categorySpending.getOrAwaitValue()
        assertEquals(value.size, list.size)
    }

    @Test
    fun `when statements flag off then hasMonthlyStatement is false`() {
        AptoUiSdk.cardOptions = CardOptions(showMonthlyStatementsOption = false)

        createSut()

        assertFalse(sut.hasMonthlyStatement.value!!)
        verify(aptoPlatform, times(0)).fetchMonthlyStatementPeriod(any())
    }

    @Test
    fun `when showMonthlyStatementsOption flag is ON then hasMonthlyStatement is defined by API in True`() {
        checkMonthlyStatementsApiResult(true)
    }

    @Test
    fun `when showMonthlyStatementsOption flag is ON then hasMonthlyStatement is defined by API in false`() {
        checkMonthlyStatementsApiResult(false)
    }

    private fun checkMonthlyStatementsApiResult(apiResult: Boolean) {
        AptoUiSdk.cardOptions = CardOptions(showMonthlyStatementsOption = true)
        val period = mock<MonthlyStatementPeriod>()
        whenever(period.contains(MONTH, YEAR)).thenReturn(apiResult)
        configurePlatformFetchMonthlyStatementPeriod(period)

        createSut()

        assertEquals(sut.hasMonthlyStatement.value!!, apiResult)
        verify(aptoPlatform).fetchMonthlyStatementPeriod(any())
    }

    private fun configurePlatformFetchMonthlyStatementPeriod(result: MonthlyStatementPeriod) {
        whenever(
            aptoPlatform.fetchMonthlyStatementPeriod(
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[0] as (Either<Failure, MonthlyStatementPeriod>) -> Unit).invoke(Either.Right(result))
        }
    }

    private fun configurePlatformCardMonthlySpending(result: MonthlySpending) {
        whenever(
            aptoPlatform.cardMonthlySpending(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyInt(),
                ArgumentMatchers.anyInt(),
                TestDataProvider.anyObject()
            )
        ).thenAnswer { invocation ->
            (invocation.arguments[3] as (Either<Failure, MonthlySpending>) -> Unit).invoke(Either.Right(result))
        }
    }
}
