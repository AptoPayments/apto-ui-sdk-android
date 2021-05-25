package com.aptopayments.sdk.features.card.statements

import com.aptopayments.mobile.data.statements.StatementMonth

internal sealed class StatementListItem {
    class YearRow(val year: Int) : StatementListItem() {
        override fun equals(other: Any?): Boolean {
            return other is YearRow && other.year == year
        }

        override fun hashCode(): Int {
            return year
        }
    }

    class MonthRow(val month: StatementMonth) : StatementListItem() {
        override fun equals(other: Any?): Boolean {
            return other is MonthRow && other.month == month
        }

        override fun hashCode(): Int {
            return month.hashCode()
        }
    }

    fun itemType(): Int {
        return when (this) {
            is YearRow -> YEAR_VIEW_TYPE
            is MonthRow -> MONTH_VIEW_TYPE
        }
    }

    companion object {
        const val YEAR_VIEW_TYPE = 0
        const val MONTH_VIEW_TYPE = 1
    }
}
