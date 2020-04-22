package com.aptopayments.sdk.ui.views.birthdate

private const val DAY_OF_MONTH_REGEX = "(0?[1-9]|[12][0-9]|3[01])"
private const val MONTH_OF_YEAR_REGEX = "(0?[1-9]|1[012])"
private const val YEAR_REGEX = "^(19|20)\\d{2}$"

internal enum class DateComponent(val length: Int, val regex: String) {
    DAY(2, DAY_OF_MONTH_REGEX),
    MONTH(2, MONTH_OF_YEAR_REGEX),
    YEAR(4, YEAR_REGEX)
}
