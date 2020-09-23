package com.aptopayments.sdk.features.card.transactionlist

import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.transaction.MCC
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.mobile.repository.transaction.TransactionListFilters
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.features.managecard.TransactionListItem
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.ArrayList
import java.util.Locale

private const val ROWS_TO_RETRIEVE = 20

internal class TransactionListViewModel constructor(
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {
    private var lastTransactionId: String? = null
    var transactionListItems: MutableLiveData<List<TransactionListItem>> = MutableLiveData()

    fun viewLoaded() {
        analyticsManager.track(Event.TransactionList)
    }

    fun fetchTransaction(
        cardId: String,
        startDate: LocalDate?,
        endDate: LocalDate?,
        mcc: MCC?,
        onComplete: (transactionsLoaded: Int) -> Unit
    ) = fetchTransactions(cardId, startDate, endDate, mcc, true, onComplete)

    fun fetchMoreTransaction(
        cardId: String,
        startDate: LocalDate?,
        endDate: LocalDate?,
        mcc: MCC?,
        onComplete: (transactionsLoaded: Int) -> Unit
    ) = fetchTransactions(cardId, startDate, endDate, mcc, false, onComplete)

    private fun fetchTransactions(
        cardId: String,
        startDate: LocalDate?,
        endDate: LocalDate?,
        mcc: MCC?,
        clearCurrent: Boolean,
        onComplete: (transactionsLoaded: Int) -> Unit
    ) {
        val filters = transactionListFilters(startDate, endDate, mcc, clearCurrent)
        AptoPlatform.fetchCardTransactions(
            cardId = cardId, filters = filters, forceRefresh = true,
            clearCachedValues = false
        ) { result ->
            result.either(::handleFailure) { transactionList ->
                handleTransactions(transactionList, clearCurrent)
                onComplete(transactionList.count())
            }
        }
    }

    private fun transactionListFilters(
        startDate: LocalDate?,
        endDate: LocalDate?,
        mcc: MCC?,
        clearCurrent: Boolean
    ): TransactionListFilters {
        val lastTransactionId = if (clearCurrent) null else this.lastTransactionId
        return TransactionListFilters(
            rows = ROWS_TO_RETRIEVE, lastTransactionId = lastTransactionId,
            startDate = startDate, endDate = endDate, mccCode = mcc?.icon?.toString()?.toLowerCase(Locale.US)
        )
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM, yyyy")
    private var currentYear = 0
    private var currentMonth = 0
    private fun handleTransactions(transactionList: List<Transaction>, clearCurrent: Boolean) {
        if (transactionList.isEmpty()) return
        lastTransactionId = transactionList.last().transactionId
        val items =
            if (clearCurrent) {
                currentYear = 0; currentMonth = 0; ArrayList()
            } else {
                transactionListItems.value as ArrayList<TransactionListItem>
            }
        transactionList.forEach { transaction ->
            val month = transaction.createdAt.monthValue
            val year = transaction.createdAt.year
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