package com.aptopayments.sdk.features.card.statements

import com.aptopayments.core.data.statements.MonthlyStatementPeriod
import com.aptopayments.core.data.statements.StatementMonth

internal class StatementListGenerator {

    var currentYear = 0

    fun generate(period: MonthlyStatementPeriod): List<StatementListItem> {
        val output = mutableListOf<StatementListItem>()

        if (period.isValid()) {
            currentYear = 0
            val monthList = period.availableMonths().reversed()
            monthList.forEach {
                addNewYearIfHasChanged(it, output)
                addMonthElement(output, it)
            }
        }

        return output
    }

    private fun addMonthElement(output: MutableList<StatementListItem>, it: StatementMonth) =
        output.add(StatementListItem.MonthRow(it))

    private fun addNewYearIfHasChanged(statementMonth: StatementMonth, output: MutableList<StatementListItem>) {
        if (currentYear != statementMonth.year) {
            currentYear = statementMonth.year
            addYearElement(output, currentYear)
        }
    }

    private fun addYearElement(output: MutableList<StatementListItem>, currentYear: Int) =
        output.add(StatementListItem.YearRow(currentYear))

}
