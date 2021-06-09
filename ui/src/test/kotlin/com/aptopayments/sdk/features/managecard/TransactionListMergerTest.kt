package com.aptopayments.sdk.features.managecard

import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.sdk.core.data.TestDataProvider
import org.junit.jupiter.api.Test
import org.threeten.bp.ZonedDateTime

internal class TransactionListMergerTest {

    val sut = TransactionListMerger()

    @Test
    fun `no update when update transactions is called with no transactions`() {
        // Given
        val currentList = emptyList<Transaction>()
        val newTransactions: MutableList<Transaction> = mutableListOf()

        // When
        val result = sut.merge(newList = newTransactions, currentList = currentList, append = false)

        // Then
        assert(result.isEmpty())
    }

    @Test
    fun `given newTransactions is empty and current transactions have one transaction when merge without appending then empty list is returned`() {
        // Given
        val oldTransaction = TestDataProvider.provideTransaction(createdAt = ZonedDateTime.now().plusMonths(1))
        val currentTransactions: MutableList<Transaction> = mutableListOf(oldTransaction)
        val newTransactions: List<Transaction> = emptyList()

        // When
        val result = sut.merge(newList = newTransactions, currentList = currentTransactions, append = false)

        // Then
        assert(result.isEmpty())
    }

    @Test
    fun `transactions are appended when update transactions is called with transactions`() {
        // Given
        val transaction = TestDataProvider.provideTransaction()
        val currentList = emptyList<Transaction>()
        val newTransactions: MutableList<Transaction> = mutableListOf(transaction)

        // When
        val result = sut.merge(newList = newTransactions, currentList = currentList, append = true)

        // Then
        assert(result == newTransactions)
        assert(result.first() == transaction)
    }

    @Test
    fun `transactions stay unchanged when newList is empty and append`() {
        // Given

        val oldTransaction = TestDataProvider.provideTransaction(transactionId = "transaction_id_123")
        val currentTransactions: MutableList<Transaction> = mutableListOf(oldTransaction)
        val newTransactions: MutableList<Transaction> = mutableListOf()

        // When
        val result = sut.merge(newList = newTransactions, currentList = currentTransactions, append = true)

        // Then
        assert(result.size == 1)
        assert(result.first() == oldTransaction)
    }

    @Test
    fun `transactions are added when update transactions is called after pull to refresh`() {
        // Given
        val transaction = TestDataProvider.provideTransaction()
        val currentList = emptyList<Transaction>()
        val newTransactions: MutableList<Transaction> = mutableListOf(transaction)

        // When
        val result = sut.merge(newList = newTransactions, currentList = currentList, append = false)

        // Then
        assert(result == newTransactions)
        assert(result.first() == transaction)
    }

    @Test
    fun `transactions are merged when update transactions is called after background refresh`() {
        // Given
        val transaction2 = TestDataProvider.provideTransaction(createdAt = ZonedDateTime.now().plusMonths(1))
        val transaction1 = TestDataProvider.provideTransaction()
        val currentTransactions: MutableList<Transaction> = mutableListOf(transaction2, transaction1)
        val newTransactions: MutableList<Transaction> = mutableListOf(transaction1)

        // When
        val result = sut.merge(newList = newTransactions, currentList = currentTransactions, append = false)

        // Then
        assert(result.size == 2)
        assert(result[0] == transaction2)
        assert(result[1] == transaction1)
    }

    @Test
    fun `transactions are replaced when update transactions is called with new transactions and previous ones are not present after background refresh`() {
        // Given
        val currentTransactions: MutableList<Transaction> = mutableListOf(TestDataProvider.provideTransaction())
        val newTransaction = TestDataProvider.provideTransaction(createdAt = ZonedDateTime.now().plusMonths(1))
        val newTransactions: MutableList<Transaction> = mutableListOf(newTransaction)

        // When
        val result = sut.merge(newList = newTransactions, currentList = currentTransactions, append = false)

        // Then
        assert(result.size == 1)
        assert(result.first() == newTransaction)
    }
}
