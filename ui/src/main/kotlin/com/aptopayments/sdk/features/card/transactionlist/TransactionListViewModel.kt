package com.aptopayments.sdk.features.card.transactionlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.transaction.MCC
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.mobile.repository.transaction.TransactionListFilters
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.managecard.TransactionListCalculatorWithHeader
import com.aptopayments.sdk.features.managecard.TransactionListItem
import org.threeten.bp.LocalDate
import java.util.Locale

private const val ROWS_TO_RETRIEVE = 20

internal class TransactionListViewModel(
    private val cardId: String,
    private val config: TransactionListConfig,
    analyticsManager: AnalyticsServiceContract,
    private val aptoPlatform: AptoPlatformProtocol
) : BaseViewModel() {
    private var lastTransaction: Transaction? = null
    private val _transactionListItems = MutableLiveData<List<TransactionListItem>>(emptyList())
    val transactionListItems = _transactionListItems as LiveData<List<TransactionListItem>>

    init {
        analyticsManager.track(Event.TransactionList)
        showLoading()
        fetchTransaction { hideLoading() }
    }

    fun fetchTransaction(onComplete: (() -> Unit)?) = fetchTransactions(false, onComplete)

    fun fetchMoreTransaction() = fetchTransactions(true, null)

    private fun fetchTransactions(isAppending: Boolean, onComplete: (() -> Unit)?) {
        val filters = transactionListFilters(config.startDate, config.endDate, config.mcc, isAppending)
        aptoPlatform.fetchCardTransactions(
            cardId = cardId, filters = filters, forceRefresh = true,
            clearCachedValues = false
        ) { result ->
            result.either(::handleFailure) { transactionList ->
                handleTransactions(transactionList, isAppending)
                onComplete?.invoke()
            }
        }
    }

    private fun transactionListFilters(
        startDate: LocalDate?,
        endDate: LocalDate?,
        mcc: MCC?,
        isAppending: Boolean
    ): TransactionListFilters {
        val transactionId = if (isAppending) this.lastTransaction?.transactionId else null
        return TransactionListFilters(
            rows = ROWS_TO_RETRIEVE, lastTransactionId = transactionId,
            startDate = startDate, endDate = endDate, mccCode = mcc?.icon?.toString()?.toLowerCase(Locale.US)
        )
    }

    private fun handleTransactions(newTransactionList: List<Transaction>, isAppending: Boolean) {
        if (newTransactionList.isNotEmpty()) {
            val calculator = TransactionListCalculatorWithHeader()
            val newList = calculator.buildList(newTransactionList, lastTransaction)
            lastTransaction = newTransactionList.last()
            val items = if (isAppending) {
                _transactionListItems.value!!.toMutableList().plus(newList)
            } else {
                newList
            }
            _transactionListItems.postValue(items)
        }
    }
}
