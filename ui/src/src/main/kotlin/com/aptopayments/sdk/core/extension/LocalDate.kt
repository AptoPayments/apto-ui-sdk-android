package com.aptopayments.sdk.core.extension

import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import java.util.*

fun LocalDate.monthToString() = this.month.getDisplayName(TextStyle.FULL, Locale.US)

fun LocalDate.yearToString() = this.year.toString()
