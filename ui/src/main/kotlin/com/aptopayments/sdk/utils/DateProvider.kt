package com.aptopayments.sdk.utils

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime

class DateProvider {

    fun localDate(): LocalDate = LocalDate.now()

    fun localDateTime(): LocalDateTime = LocalDateTime.now()
}
