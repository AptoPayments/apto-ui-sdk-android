package com.aptopayments.sdk.features.card.transactionlist

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import com.aptopayments.core.data.transaction.MCC
import com.aptopayments.core.data.transaction.Transaction
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.extension.getMonthYear
import com.aptopayments.sdk.features.managecard.TransactionListItem
import com.aptopayments.core.repository.transaction.TransactionListFilters
import org.threeten.bp.LocalDate
import java.text.SimpleDateFormat
import javax.inject.Inject

private const val ROWS_TO_RETRIEVE = 20

internal class TransactionListViewModel @Inject constructor(
        private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {
    private var lastTransactionId: String? = null
    var transactionListItems: MutableLiveData<List<TransactionListItem>> = MutableLiveData()

    fun viewLoaded() {
        analyticsManager.track(Event.TransactionList)
    }

    fun fetchTransaction(cardId: String, startDate: LocalDate?, endDate: LocalDate?, mcc: MCC?,
                         onComplete: (transactionsLoaded: Int) -> Unit) =
            fetchTransactions(cardId, startDate, endDate, mcc, true, onComplete)

    fun fetchMoreTransaction(cardId: String, startDate: LocalDate?, endDate: LocalDate?, mcc: MCC?,
                             onComplete: (transactionsLoaded: Int) -> Unit) =
            fetchTransactions(cardId, startDate, endDate, mcc, false, onComplete)

    private fun fetchTransactions(cardId: String, startDate: LocalDate?, endDate: LocalDate?, mcc: MCC?,
                                  clearCurrent: Boolean, onComplete: (transactionsLoaded: Int) -> Unit) {
        val filters = transactionListFilters(startDate, endDate, mcc, clearCurrent)
        AptoPlatform.fetchCardTransactions(cardId = cardId, filters = filters, forceRefresh = true,
                clearCachedValues = false) { result ->
            result.either(::handleFailure) { transactionList ->
                handleTransactions(transactionList, clearCurrent)
                onComplete(transactionList.count())
            }
        }
    }

    private fun transactionListFilters(startDate: LocalDate?, endDate: LocalDate?, mcc: MCC?,
                                       clearCurrent: Boolean): TransactionListFilters {
        val lastTransactionId = if (clearCurrent) null else this.lastTransactionId
        return TransactionListFilters(rows = ROWS_TO_RETRIEVE, lastTransactionId = lastTransactionId,
                startDate = startDate, endDate = endDate, mccCode = mcc?.icon?.toString()?.toLowerCase())
    }

    @SuppressLint("SimpleDateFormat")
    private val dateFormatter = SimpleDateFormat("MMMM, yyyy")
    private var currentYear = 0
    private var currentMonth = 0
    private fun handleTransactions(transactionList: List<Transaction>, clearCurrent: Boolean) {
        if (transactionList.isEmpty()) return
        lastTransactionId = transactionList.last().transactionId
        val items =
                if (clearCurrent) { currentYear = 0; currentMonth = 0; ArrayList() }
                else transactionListItems.value as ArrayList<TransactionListItem>
        transactionList.forEach { transaction ->
            val (month, year) = transaction.createdAt.getMonthYear()
            if (month != currentMonth || year != currentYear) {
                items.add(TransactionListItem.SectionHeader(dateFormatter.format(transaction.createdAt)))
                currentMonth = month
                currentYear = year
            }
            items.add(TransactionListItem.TransactionRow(transaction))
        }
        transactionListItems.postValue(items)
    }
}
