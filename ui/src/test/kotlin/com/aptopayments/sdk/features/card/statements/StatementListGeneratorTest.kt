package com.aptopayments.sdk.features.card.statements

import com.aptopayments.mobile.data.statements.MonthlyStatementPeriod
import com.aptopayments.mobile.data.statements.StatementMonth
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private val MONTH_8_2019 = StatementMonth(8, 2019)
private val MONTH_9_2019 = StatementMonth(9, 2019)
private val MONTH_10_2019 = StatementMonth(10, 2019)
private val MONTH_11_2019 = StatementMonth(11, 2019)

class StatementListGeneratorTest {

    private val generator = StatementListGenerator()
    private val endMonth = MONTH_10_2019

    @Test
    fun `when period is not valid then empty list`() {
        val start = MONTH_11_2019
        val period = MonthlyStatementPeriod(start, endMonth)

        assert(generator.generate(period).isEmpty())
    }

    @Test
    fun `when period is same month then year and one element in list`() {
        val start = MONTH_10_2019
        val period = MonthlyStatementPeriod(start, endMonth)
        val generatedPeriod = generator.generate(period)
        val list = listOf(StatementListItem.YearRow(2019), StatementListItem.MonthRow(start))

        assertEquals(list, generatedPeriod)
    }

    @Test
    fun `when period is x months in same year then x+1 element in list`() {
        val start = MONTH_8_2019
        val period = MonthlyStatementPeriod(start, endMonth)

        val generatedPeriod = generator.generate(period)

        val list = listOf(
            StatementListItem.YearRow(2019),
            StatementListItem.MonthRow(MONTH_10_2019),
            StatementListItem.MonthRow(MONTH_9_2019),
            StatementListItem.MonthRow(MONTH_8_2019)
        )
        assertEquals(list, generatedPeriod)
    }

    @Test
    fun `when period has different years then list generated correctly`() {
        val start = StatementMonth(12, 2018)
        val end = StatementMonth(2, 2019)

        val period = MonthlyStatementPeriod(start, end)

        val generatedPeriod = generator.generate(period)

        val list = listOf(
            StatementListItem.YearRow(2019),
            StatementListItem.MonthRow(StatementMonth(2, 2019)),
            StatementListItem.MonthRow(StatementMonth(1, 2019)),
            StatementListItem.YearRow(2018),
            StatementListItem.MonthRow(StatementMonth(12, 2018))
        )
        assertEquals(list, generatedPeriod)
    }
}
