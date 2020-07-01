package com.aptopayments.sdk.ui.views.birthdate

import org.threeten.bp.LocalDate

class BirthdateViewDateParser {
    fun parse(year: String, month: String, day: String): LocalDate? {
        return try {
            LocalDate.of(year.toInt(), month.toInt(), day.toInt())
        } catch (e: Exception) {
            null
        }
    }
}
