package com.aptopayments.sdk.features.managecard

import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.mobile.repository.transaction.TransactionListFilters

class FetchTransactionsTaskQueue(private val aptoPlatformProtocol: AptoPlatformProtocol) {
    private var loadOperation: FetchTransactionsTask? = null
    private var loadMoreOperation: FetchTransactionsTask? = null
    private var backgroundRefreshOperation: FetchTransactionsTask? = null
    private val stateList: ArrayList<String>
        get() {
            val stateList = arrayListOf("complete")
            if (aptoPlatformProtocol.isShowDetailedCardActivityEnabled()) {
                stateList.add("declined")
            }
            return stateList
        }

    private fun isLoadInProgress(): Boolean = loadOperation?.isExecuting ?: false
    private fun isLoadMoreInProgress(): Boolean = loadMoreOperation?.isExecuting ?: false
    private fun isBackgroundRefreshInProgress(): Boolean = backgroundRefreshOperation?.isExecuting ?: false

    fun loadTransactions(
        cardID: String,
        rows: Int = 20,
        forceApiCall: Boolean,
        clearCachedValue: Boolean,
        onComplete: ((Either<Failure, List<Transaction>>) -> Unit)
    ) {
        if (isLoadInProgress()) return
        loadMoreOperation?.cancel()
        val params = FetchTransactionsTask.Params(
            cardId = cardID,
            filters = TransactionListFilters(rows = rows, state = stateList),
            forceApiCall = forceApiCall,
            clearCachedValues = clearCachedValue
        )
        val task = FetchTransactionsTask(params, onComplete)
        loadOperation = task
        task.start()
    }

    fun loadMoreTransactions(
        cardID: String,
        lastTransactionId: String?,
        rows: Int = 20,
        onComplete: ((Either<Failure, List<Transaction>>) -> Unit)
    ) {
        if (isLoadInProgress() || isLoadMoreInProgress()) return
        val params = FetchTransactionsTask.Params(
            cardId = cardID,
            filters = TransactionListFilters(rows = rows, lastTransactionId = lastTransactionId, state = stateList),
            forceApiCall = true,
            clearCachedValues = false
        )
        val task = FetchTransactionsTask(params, onComplete)
        loadMoreOperation = task
        task.start()
    }

    fun backgroundRefresh(cardID: String, rows: Int = 20, onComplete: ((Either<Failure, List<Transaction>>) -> Unit)) {
        if (isLoadInProgress() || isLoadMoreInProgress() || isBackgroundRefreshInProgress()) return
        val params = FetchTransactionsTask.Params(
            cardId = cardID,
            filters = TransactionListFilters(rows = rows, state = stateList),
            forceApiCall = true,
            clearCachedValues = false
        )
        val task = FetchTransactionsTask(params, onComplete)
        backgroundRefreshOperation = task
        task.start()
    }

    fun cancel() {
        loadOperation?.cancel()
        loadMoreOperation?.cancel()
        backgroundRefreshOperation?.cancel()
    }
}
