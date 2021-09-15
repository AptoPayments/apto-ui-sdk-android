package com.aptopayments.sdk.utils.extensions

import org.threeten.bp.DayOfWeek
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

private val monthDayFormatter by lazy { DateTimeFormatter.ofPattern("MMM d", Locale.US) }

fun LocalDate.nonWeekendDaysUntilToday() = this.nonWeekendDaysUntil(LocalDate.now())

fun LocalDate.nonWeekendDaysUntil(value: LocalDate): Int {
    var first: LocalDate
    val second: LocalDate

    if (this.isAfter(value)) {
        first = value
        second = this
    } else {
        first = this
        second = value
    }

    var count = 0
    while (first != second) {

        first = first.plusDays(1)
        if (first.dayOfWeek != DayOfWeek.SATURDAY && first.dayOfWeek != DayOfWeek.SUNDAY) {
            count++
        }
    }

    return count
}

fun LocalDate.plusWorkingDays(days: Int): LocalDate {
    require(days >= 0)

    var output = this

    var count = 0
    while (count < days) {

        output = output.plusDays(1)
        if (output.dayOfWeek != DayOfWeek.SATURDAY && output.dayOfWeek != DayOfWeek.SUNDAY) {
            count++
        }
    }

    return output
}

internal fun LocalDate.formatMonthDay(): String {
    return monthDayFormatter.format(this).replace(".", "").replace(",", "").toCapitalized()
}
