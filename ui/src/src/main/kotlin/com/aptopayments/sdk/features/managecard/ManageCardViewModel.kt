package com.aptopayments.sdk.features.managecard

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.MutableLiveData
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.card.CardDetails
import com.aptopayments.core.data.card.CardStyle
import com.aptopayments.core.data.card.Money
import com.aptopayments.core.data.cardproduct.CardProduct
import com.aptopayments.core.data.fundingsources.Balance
import com.aptopayments.core.data.transaction.Transaction
import com.aptopayments.core.extension.add
import com.aptopayments.core.extension.getMonthYear
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.core.repository.transaction.FetchTransactionsTaskQueue
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import java.lang.reflect.Modifier
import java.text.SimpleDateFormat
import java.util.*

internal class ManageCardViewModel constructor(
        private val getTransactionsQueue: FetchTransactionsTaskQueue,
        private var analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    var card: MutableLiveData<Card> = MutableLiveData()
    var cardLoaded: MutableLiveData<Boolean> = MutableLiveData()
    var state: MutableLiveData<Card.CardState?> = MutableLiveData()
    var orderedStatus: MutableLiveData<Card.OrderedStatus> = MutableLiveData()
    var cardStyle: MutableLiveData<CardStyle?> = MutableLiveData()
    var cardHolder: MutableLiveData<String?> = MutableLiveData()
    var lastFour: MutableLiveData<String?> = MutableLiveData()
    var cardNetwork: MutableLiveData<Card.CardNetwork?> = MutableLiveData()
    var spendableToday: MutableLiveData<Money?> = MutableLiveData()
    var nativeSpendableToday: MutableLiveData<Money?> = MutableLiveData()
    var showPhysicalCardActivationMessage: MutableLiveData<Boolean> = MutableLiveData()
    var cardInfoVisible: MutableLiveData<Boolean?> = MutableLiveData()
    var pan: MutableLiveData<String?> = MutableLiveData()
    var cvv: MutableLiveData<String?> = MutableLiveData()
    var expirationMonth: MutableLiveData<Int?> = MutableLiveData()
    var expirationYear: MutableLiveData<Int?> = MutableLiveData()
    var transactions: MutableLiveData<List<Transaction>?> = MutableLiveData()
    var fundingSource: MutableLiveData<Balance?> = MutableLiveData()
    var transactionListItems: MutableLiveData<List<TransactionListItem>> = MutableLiveData(listOf())
    var cardProduct: MutableLiveData<CardProduct> = MutableLiveData()
    var transactionsInfoRetrieved: MutableLiveData<Boolean> = MutableLiveData()

    private val rowsPerPage = 20
    private var lastTransactionId: String? = null
    private var cardInfoRetrieved = false
    var balanceLoaded = false
    @SuppressLint("SimpleDateFormat")
    private val dateFormatter = SimpleDateFormat("MMMM, yyyy")

    fun viewLoaded() {
        analyticsManager.track(Event.ManageCard)
    }

    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    var cardInfoDataTimeout: Date? = null

    fun viewReady(cardId: String) {
        cardInfoDataTimeout?.let { timeout ->
            if (Date().after(timeout)) {
                pan.postValue(null)
                cvv.postValue(null)
                expirationMonth.postValue(null)
                expirationYear.postValue(null)
                cardInfoVisible.postValue(false)
            }
        }
        transactionsInfoRetrieved.postValue(false)
        fetchData(cardId, forceApiCall = false, clearCachedValue = false) {
            backgroundRefresh(cardId = cardId)
        }
    }

    fun refreshData(cardId: String, onComplete: (() -> Unit)) {
        transactionsInfoRetrieved.postValue(false)
        fetchData(cardId = cardId, forceApiCall = true, clearCachedValue = true, onComplete = onComplete)
    }

    fun refreshTransactions(cardId: String, onComplete: (() -> Unit)) {
        getTransactions(cardId = cardId, forceApiCall = true, clearCachedValue = true) {
            transactionsInfoRetrieved.postValue(true)
            onComplete()
        }
    }

    private fun fetchData(cardId: String, forceApiCall: Boolean, clearCachedValue: Boolean, onComplete: () -> Unit) {
        getCard(cardId = cardId, refresh = forceApiCall) { card ->
            card.cardProductID?.let {
                getCardProduct(cardProductId = it) {
                    getCardBalance(cardId = cardId, refresh = forceApiCall) {
                        getTransactions(cardId = cardId, forceApiCall = forceApiCall, clearCachedValue = clearCachedValue) {
                            transactionsInfoRetrieved.postValue(true)
                            onComplete()
                        }
                    }
                }
            }
        }
    }

    private fun backgroundRefresh(cardId: String) {
        getCard(cardId = cardId, refresh = true) { cardInfoRetrieved = true }
        getCardBalance(cardId, refresh = true) {}
        getBackgroundTransactions(cardId = cardId) { transactionsInfoRetrieved.postValue(true) }
    }

    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    fun getCardProduct(cardProductId: String, onComplete: (() -> Unit)? = null) {
        AptoPlatform.fetchCardProduct(cardProductId, false) {
            it.either(::handleFailure) { cardProduct ->
                this.cardProduct.postValue(cardProduct)
                onComplete?.invoke()
                Unit
            }
        }
    }

    //
    // Card operations
    //
    fun refreshCard(cardId: String) {
        getCard(cardId, refresh = true) {}
    }

    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    fun getCard(cardId: String, refresh: Boolean, onComplete: ((Card) -> Unit)? = null) {
        AptoPlatform.fetchFinancialAccount(accountId = cardId, showDetails = false, forceRefresh = refresh) { result ->
            result.either(::handleFailure) { card ->
                updateViewModelWithCard(card)
                onComplete?.invoke(card)
                Unit
            }
        }
    }

    private fun updateViewModelWithCard(card: Card) {
        this.card.postValue(card)
        cardHolder.postValue(card.cardHolder)
        lastFour.postValue(card.lastFourDigits)
        cardNetwork.postValue(card.cardNetwork)
        state.postValue(card.state)
        orderedStatus.postValue(card.orderedStatus)

        showPhysicalCardActivationMessage
            .postValue(card.orderedStatus == Card.OrderedStatus.ORDERED && card.orderedStatus != orderedStatus.value)

        spendableToday.postValue(card.spendableAmount)
        nativeSpendableToday.postValue(card.nativeSpendableAmount)
        cardStyle.postValue(card.cardStyle)
        if (cardLoaded.value == false) cardLoaded.postValue(true)
        if (transactionListItems.value.isNullOrEmpty()) {
            val list = ArrayList<TransactionListItem>()
            list.add(TransactionListItem.HeaderView)
            transactionListItems.postValue(list)
        }
    }

    private fun handleCardBalance(balance: Balance) {
        fundingSource.postValue(balance)
    }

    internal fun cardDetailsChanged(cardDetails: CardDetails?) {
        pan.postValue(cardDetails?.pan)
        cvv.postValue(cardDetails?.cvv)
        val components = cardDetails?.expirationDate?.split("-")
        expirationYear.postValue(components?.first()?.toInt()?.let { year ->
            if (year > 2000) year - 2000 else year
        })
        expirationMonth.postValue(components?.last()?.toInt())
        cardInfoVisible.postValue(cardDetails != null)
        cardInfoDataTimeout = Date().add(Calendar.MINUTE, 2)
    }

    fun refreshBalance(cardId: String) {
        getCardBalance(cardId, refresh = true) {}
    }

    private fun getCardBalance(cardId: String, refresh: Boolean, onComplete: (() -> Unit)? = null) {
        // TODO: use refresh = false to read from cache
        AptoPlatform.fetchCardFundingSource(cardId, refresh) {
            balanceLoaded = true
            it.either(::handleFailure, ::handleCardBalance)
            onComplete?.invoke()
        }
    }

    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    fun getTransactions(cardId: String, forceApiCall: Boolean, clearCachedValue: Boolean, onComplete: () -> Unit) {
        getTransactionsQueue.loadTransactions(cardId, rowsPerPage, forceApiCall, clearCachedValue) { result ->
            result.either(::handleFailure) { transactionList ->
                if (clearCachedValue) transactions.value = null
                val updatedTransactions = updateTransactions(transactionList, append = false)
                updateTransactionItems(updatedTransactions, append = false, clearCachedValue = clearCachedValue)
                updatedTransactions.lastOrNull()?.let { lastTransactionId = it.transactionId }
                onComplete()
            }
        }
    }

    fun getMoreTransactions(cardId: String, onComplete: (Int) -> Unit) {
        getTransactionsQueue.loadMoreTransactions(cardId, lastTransactionId, rowsPerPage) { result ->
            result.either(::handleFailure) { transactionList ->
                val updatedTransactions = updateTransactions(transactionList, append = true)
                if (updatedTransactions.isNotEmpty()) {
                    updateTransactionItems(updatedTransactions, append = true, clearCachedValue = false)
                }
                transactionList.lastOrNull()?.let { lastTransactionId = it.transactionId }
                onComplete(transactionList.size)
            }
        }
    }

    private fun getBackgroundTransactions(cardId: String, onComplete: () -> Unit) {
        getTransactionsQueue.backgroundRefresh(cardId, rowsPerPage) { result ->
            result.either(::handleFailure) { transactionList ->
                val updatedTransactions = updateTransactions(transactionList, append = false)
                if (updatedTransactions.isNotEmpty()) {
                    updateTransactionItems(updatedTransactions, append = false, clearCachedValue = false)
                }
                updatedTransactions.lastOrNull()?.let { lastTransactionId = it.transactionId }
                onComplete()
            }
        }
    }

    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    fun updateTransactions(transactionList: List<Transaction>, append: Boolean) : List<Transaction> {
        if (transactionList.isEmpty()) return transactionList
        val currentTransactions: ArrayList<Transaction> = transactions.value as? ArrayList<Transaction>
                ?: ArrayList()

        if (append) {
            currentTransactions.addAll(transactionList)
            transactions.postValue(currentTransactions)
            return transactionList
        }

        if (currentTransactions.isEmpty() || transactionList.last().createdAt.after(currentTransactions.first().createdAt)) {
            transactions.postValue(transactionList)
            return transactionList
        }

        // Background refresh
        var newTransactionIndex = 0
        val topCachedTransactionDate = currentTransactions.first().createdAt
        while (newTransactionIndex<transactionList.size && transactionList[newTransactionIndex].createdAt.after(topCachedTransactionDate)) {
            currentTransactions.add(0, transactionList[newTransactionIndex])
            newTransactionIndex++
        }
        transactions.postValue(currentTransactions)
        return currentTransactions
    }

    private fun updateTransactionItems(newTransactions: List<Transaction>, append: Boolean, clearCachedValue: Boolean) {
        if (clearCachedValue) processNewTransactions(newTransactions)
        else {
            val currentTransactionItems: ArrayList<TransactionListItem> = transactionListItems.value as ArrayList<TransactionListItem>
            mergeListItems(newTransactions, currentTransactionItems, append)
        }
    }

    private fun processNewTransactions(newTransactions: List<Transaction>) {
        val transactionItemsArrayList: MutableList<TransactionListItem> = mutableListOf()
        transactionItemsArrayList.add(TransactionListItem.HeaderView)
        mergeListItems(newTransactions, transactionItemsArrayList, append = false)
    }

    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    fun buildItems(newTransactions: List<Transaction>, skipFirstHeader: Boolean = false) : ArrayList<TransactionListItem> {
        val result = ArrayList<TransactionListItem>()
        var transactionYear = -1
        var transactionMonth = -1
        if (skipFirstHeader) {
            val (month, year) = newTransactions.first().createdAt.getMonthYear()
            transactionMonth = month
            transactionYear = year
        }

        var currentTransactionYear: Int
        var currentTransactionMonth: Int
        newTransactions.forEach{
            val (month, year) = it.createdAt.getMonthYear()
            currentTransactionMonth = month
            currentTransactionYear = year
            if (transactionYear != currentTransactionYear || transactionMonth != currentTransactionMonth) {
                result.add(TransactionListItem.SectionHeader(dateFormatter.format(it.createdAt)))
                transactionYear = currentTransactionYear
                transactionMonth = currentTransactionMonth
            }
            result.add(TransactionListItem.TransactionRow(it))
        }
        return result
    }

    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    fun mergeListItems(newTransactions: List<Transaction>, currentTransactionItems: MutableList<TransactionListItem>, append: Boolean) {
        if (append) {
            val lastCurrentTransaction = currentTransactionItems.findLast {
                it.itemType() == TransactionListItem.TRANSACTION_ROW_VIEW_TYPE
            } as TransactionListItem.TransactionRow
            val (currentMonth, currentYear) = lastCurrentTransaction.transaction.createdAt.getMonthYear()
            val (newTransactionMonth, newTransactionYear) = newTransactions.first().createdAt.getMonthYear()
            val skipFirstHeader = currentMonth == newTransactionMonth && currentYear == newTransactionYear
            currentTransactionItems.addAll(buildItems(newTransactions, skipFirstHeader = skipFirstHeader))
            transactionListItems.postValue(currentTransactionItems)
            return
        }

        if (currentTransactionItems.size < 2) {
            currentTransactionItems.addAll(buildItems(newTransactions, skipFirstHeader = false))
            transactionListItems.postValue(currentTransactionItems)
            return
        }

        val mostRecentCurrentTransaction = (currentTransactionItems.find {
            it.itemType() == TransactionListItem.TRANSACTION_ROW_VIEW_TYPE
        } as TransactionListItem.TransactionRow).transaction
        val oldestNewTransaction = newTransactions.last()
        val result: ArrayList<TransactionListItem> = ArrayList()

        // First item is card line item
        result.add(currentTransactionItems.first())
        result.addAll(buildItems(newTransactions))

        if (oldestNewTransaction.createdAt <= mostRecentCurrentTransaction.createdAt) {
            // Find the point where the new transactions overlap with the old ones
            val index = currentTransactionItems.indexOfFirst { transactionItem ->
                transactionItem.itemType() == TransactionListItem.TRANSACTION_ROW_VIEW_TYPE &&
                        (transactionItem as TransactionListItem.TransactionRow).transaction.createdAt <= oldestNewTransaction.createdAt
            }
            if (index != -1) result.addAll(currentTransactionItems.subList(index, currentTransactionItems.size-1))
        }
        transactionListItems.postValue(result)
    }
}
