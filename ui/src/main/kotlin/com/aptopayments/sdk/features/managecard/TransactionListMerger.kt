package com.aptopayments.sdk.features.managecard

import com.aptopayments.mobile.data.transaction.Transaction

internal class TransactionListMerger {

    fun merge(
        newList: List<Transaction>,
        currentList: List<Transaction>?,
        append: Boolean,
        clearCachedValue: Boolean = false
    ): List<Transaction> {
        val currentTransactions = getCurrentList(currentList, clearCachedValue)
        return when {
            append -> currentTransactions.plus(newList)
            shouldOnlyKeepNewList(currentTransactions, newList) -> newList
            else -> processBackgroundRefresh(currentTransactions, newList)
        }
    }

    private fun getCurrentList(
        currentList: List<Transaction>?,
        clearCachedValue: Boolean
    ) = if (currentList == null || clearCachedValue) {
        mutableListOf()
    } else {
        currentList.toMutableList()
    }

    private fun processBackgroundRefresh(
        currentTransactions: MutableList<Transaction>,
        newList: List<Transaction>
    ): MutableList<Transaction> {
        var newTransactionIndex = 0
        val topCachedTransactionDate = currentTransactions.first().createdAt
        val newListToAddAtTheBeginning = mutableListOf<Transaction>()
        while (newTransactionIndex < newList.size &&
            newList[newTransactionIndex].createdAt.isAfter(topCachedTransactionDate)
        ) {
            newListToAddAtTheBeginning.add(newList[newTransactionIndex])
            newTransactionIndex++
        }
        currentTransactions.addAll(0, newListToAddAtTheBeginning)

        return currentTransactions
    }

    private fun shouldOnlyKeepNewList(
        currentTransactions: MutableList<Transaction>,
        newList: List<Transaction>
    ) = currentTransactions.isEmpty() ||
        newList.isEmpty() ||
        newList.last().createdAt.isAfter(currentTransactions.first().createdAt)
}
