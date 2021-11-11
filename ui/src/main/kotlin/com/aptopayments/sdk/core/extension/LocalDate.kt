package com.aptopayments.sdk.core.extension

import com.aptopayments.sdk.utils.extensions.toCapitalized
import org.threeten.bp.LocalDate
import org.threeten.bp.format.TextStyle
import java.util.Locale

fun LocalDate.yearToString() = this.year.toString()

fun LocalDate.monthLocalized(): String = this.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).toCapitalized()
