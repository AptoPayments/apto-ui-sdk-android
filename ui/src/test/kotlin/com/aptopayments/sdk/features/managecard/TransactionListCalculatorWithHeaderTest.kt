package com.aptopayments.sdk.features.managecard

import com.aptopayments.sdk.core.data.TestDataProvider
import org.junit.jupiter.api.Test
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

class TransactionListCalculatorWithHeaderTest {

    private val sut = TransactionListCalculatorWithHeader()

    @Test
    fun `build items does not add section header when last transaction has same month & year`() {
        // Given
        val date = ZonedDateTime.of(2020, 11, 25, 11, 7, 0, 0, ZoneId.systemDefault())

        val previousTransaction = TestDataProvider.provideTransaction(createdAt = date)
        val newTransaction = TestDataProvider.provideTransaction(createdAt = date)
        val newTransactions = mutableListOf(newTransaction)

        // When
        val result = sut.buildList(newTransactions, previousTransaction)

        // Then
        assert(result.size == 1)
        assert(result.first().itemType() == TransactionListItem.TRANSACTION_ROW_VIEW_TYPE)
        assert((result.first() as TransactionListItem.TransactionRow).transaction == newTransaction)
    }

    @Test
    fun `build items does adds section header when last transaction doesn't have same month`() {
        // Given
        val date = ZonedDateTime.of(2020, 11, 25, 11, 7, 0, 0, ZoneId.systemDefault())

        val previousTransaction = TestDataProvider.provideTransaction(createdAt = date)
        val newTransaction = TestDataProvider.provideTransaction(createdAt = date.plusMonths(1))
        val newTransactions = mutableListOf(newTransaction)

        // When
        val result = sut.buildList(newTransactions, previousTransaction)

        // Then
        assert(result.size == 2)
        assert(result.first().itemType() == TransactionListItem.SECTION_HEADER_VIEW_TYPE)
        assert(result[1].itemType() == TransactionListItem.TRANSACTION_ROW_VIEW_TYPE)
        assert((result[1] as TransactionListItem.TransactionRow).transaction == newTransaction)
    }
}
