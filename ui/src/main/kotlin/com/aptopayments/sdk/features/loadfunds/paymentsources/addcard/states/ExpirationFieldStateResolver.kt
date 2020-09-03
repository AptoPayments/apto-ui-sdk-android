package com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.states

import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.FieldState
import com.aptopayments.sdk.utils.DateProvider
import org.threeten.bp.LocalDate

internal class ExpirationFieldStateResolver(private val dateProvider: DateProvider) {

    operator fun invoke(expiration: String?): FieldState {
        return try {
            parseExpirationAndCheckForValidity(expiration)
        } catch (e: Exception) {
            FieldState.ERROR
        }
    }

    private fun parseExpirationAndCheckForValidity(expiration: String?): FieldState {
        return if (expiration?.length == 4) {
            checkIfDateIsOlderThanCurrentMonth(parseDate(expiration))
        } else {
            FieldState.TYPING
        }
    }

    private fun checkIfDateIsOlderThanCurrentMonth(lastExpirationDate: LocalDate): FieldState {
        return if (lastExpirationDate.isBefore(getLastDayOfCurrentMonth())) {
            FieldState.ERROR
        } else {
            FieldState.CORRECT
        }
    }

    private fun parseDate(expiration: String): LocalDate {
        return LocalDate.of(getYear(expiration), getMonth(expiration), 1)
            .plusMonths(1)
            .minusDays(1)
    }

    private fun getMonth(expiration: String) = expiration.take(2).toInt()

    private fun getYear(expiration: String) = ("20" + expiration.takeLast(2)).toInt()

    private fun getLastDayOfCurrentMonth(): LocalDate {
        val today = dateProvider.localDate()
        return LocalDate.of(today.year, today.month, 1)
    }
}
