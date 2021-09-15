package com.aptopayments.sdk.utils.extensions

import com.aptopayments.mobile.data.card.Card
import org.threeten.bp.LocalDate

private const val MAX_DAYS_TO_SHOW_FAKE_CARD = 15

fun Card.shouldShowFakeShippingStatus(today: LocalDate): Boolean {
    return orderedStatus == Card.OrderedStatus.ORDERED &&
        issuedAt != null &&
        (issuedAt?.toLocalDate()?.nonWeekendDaysUntil(today) ?: Int.MAX_VALUE <= MAX_DAYS_TO_SHOW_FAKE_CARD)
}
