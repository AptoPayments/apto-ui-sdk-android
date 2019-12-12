package com.aptopayments.sdk.utils

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class DateProvider {

    fun localDate() = LocalDate.now()

    fun localDateTime() = LocalDateTime.now()

    fun zonedDateTime(zone: ZoneId = ZoneId.systemDefault()) = ZonedDateTime.now(ZoneId.systemDefault())

}
