package com.aptopayments.sdk.features.managecard

import com.aptopayments.mobile.data.transaction.Transaction
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter

class TransactionListCalculatorWithHeader {

    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM, yyyy")

    fun buildList(
        newTransactions: List<Transaction>,
        lastTransaction: Transaction? = null
    ): List<TransactionListItem> {
        val output = mutableListOf<TransactionListItem>()

        var previousHeaderDate = calculatePreviousHeaderDate(newTransactions, lastTransaction)

        newTransactions.forEach {
            if (haveSameDateAndYear(previousHeaderDate, it.createdAt)) {
                output.add(createHeaderWithTransactionDate(it))
                previousHeaderDate = it.createdAt
            }
            output.add(TransactionListItem.TransactionRow(it))
        }
        return output
    }

    private fun calculatePreviousHeaderDate(
        newTransactions: List<Transaction>,
        lastTransaction: Transaction?
    ): ZonedDateTime? {
        return if (shouldSkipHeader(lastTransaction, newTransactions.firstOrNull())) {
            newTransactions.firstOrNull()?.createdAt
        } else {
            null
        }
    }

    private fun shouldSkipHeader(lastTransaction: Transaction?, firstNewTransaction: Transaction?): Boolean {
        return lastTransaction?.createdAt?.monthValue == firstNewTransaction?.createdAt?.monthValue &&
            lastTransaction?.createdAt?.year == firstNewTransaction?.createdAt?.year
    }

    private fun createHeaderWithTransactionDate(it: Transaction) =
        TransactionListItem.SectionHeader(it.createdAt.format(dateFormatter))

    private fun haveSameDateAndYear(
        previousTransaction: ZonedDateTime?,
        date: ZonedDateTime
    ) = previousTransaction?.year != date.year || previousTransaction.monthValue != date.monthValue
}
