package com.aptopayments.sdk.features.managecard

import androidx.annotation.VisibleForTesting
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.card.CardDetails
import com.aptopayments.core.data.card.CardStyle
import com.aptopayments.core.data.card.Money
import com.aptopayments.core.data.cardproduct.CardProduct
import com.aptopayments.core.data.fundingsources.Balance
import com.aptopayments.core.data.transaction.Transaction
import com.aptopayments.core.functional.getOrElse
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.core.repository.transaction.FetchTransactionsTaskQueue
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.core.usecase.FetchLocalCardDetailsUseCase
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.repository.IAPHelper
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf
import org.threeten.bp.format.DateTimeFormatter
import java.lang.reflect.Modifier
import java.util.ArrayList

private const val ROWS_PER_PAGE = 20

internal class ManageCardViewModel constructor(
    private val cardId: String,
    private val getTransactionsQueue: FetchTransactionsTaskQueue,
    private var analyticsManager: AnalyticsServiceContract,
    private val aptoUiSdkProtocol: AptoUiSdkProtocol
) : BaseViewModel(), KoinComponent {

    private val fetchLocalCardDetailsUseCase: FetchLocalCardDetailsUseCase by inject()
    private val iapHelper: IAPHelper by inject { parametersOf(cardId) }

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
    val cardInfo: LiveData<CardDetails?> = getCardDetailsLiveData()
    var transactions: MutableLiveData<List<Transaction>?> = MutableLiveData()
    var fundingSource: MutableLiveData<Balance?> = MutableLiveData()
    var transactionListItems: MutableLiveData<List<TransactionListItem>> = MutableLiveData(listOf())
    var cardProduct: MutableLiveData<CardProduct> = MutableLiveData()
    var transactionsInfoRetrieved: MutableLiveData<Boolean> = MutableLiveData()
    val showAddToGooglePay = Transformations.map(iapHelper.showAddCardButton) { showAddCardButton ->
        aptoUiSdkProtocol.cardOptions.inAppProvisioningEnabled() && iapHelper.satisfyHardwareRequisites() && showAddCardButton
    }

    private var lastTransactionId: String? = null
    private var cardInfoRetrieved = false
    var balanceLoaded = false
    private val dateFormatter = DateTimeFormatter.ofPattern("MMMM, yyyy")

    init {
        startIapHelper()
    }

    fun viewLoaded() {
        analyticsManager.track(Event.ManageCard)
    }

    fun viewReady() {
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

    private fun getCardDetailsLiveData() =
        fetchLocalCardDetailsUseCase().getOrElse { MutableLiveData<CardDetails?>(null) }

    private fun fetchData(cardId: String, forceApiCall: Boolean, clearCachedValue: Boolean, onComplete: () -> Unit) {
        getCard(cardId = cardId, refresh = forceApiCall) { card ->
            card.cardProductID?.let {
                getCardProduct(cardProductId = it) {
                    getCardBalance(cardId = cardId, refresh = forceApiCall) {
                        getTransactions(
                            cardId = cardId,
                            forceApiCall = forceApiCall,
                            clearCachedValue = clearCachedValue
                        ) {
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
        AptoPlatform.fetchFinancialAccount(accountId = cardId, forceRefresh = refresh) { result ->
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
        getTransactionsQueue.loadTransactions(cardId, ROWS_PER_PAGE, forceApiCall, clearCachedValue) { result ->
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
        getTransactionsQueue.loadMoreTransactions(cardId, lastTransactionId, ROWS_PER_PAGE) { result ->
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
        getTransactionsQueue.backgroundRefresh(cardId, ROWS_PER_PAGE) { result ->
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
    fun updateTransactions(transactionList: List<Transaction>, append: Boolean): List<Transaction> {
        if (transactionList.isEmpty()) return transactionList
        val currentTransactions: ArrayList<Transaction> = transactions.value as? ArrayList<Transaction>
            ?: ArrayList()

        if (append) {
            currentTransactions.addAll(transactionList)
            transactions.postValue(currentTransactions)
            return transactionList
        }

        if (currentTransactions.isEmpty() || transactionList.last().createdAt.isAfter(currentTransactions.first().createdAt)) {
            transactions.postValue(transactionList)
            return transactionList
        }

        // Background refresh
        var newTransactionIndex = 0
        val topCachedTransactionDate = currentTransactions.first().createdAt
        while (newTransactionIndex < transactionList.size &&
            transactionList[newTransactionIndex].createdAt.isAfter(topCachedTransactionDate)
        ) {
            currentTransactions.add(0, transactionList[newTransactionIndex])
            newTransactionIndex++
        }
        transactions.postValue(currentTransactions)
        return currentTransactions
    }

    private fun updateTransactionItems(newTransactions: List<Transaction>, append: Boolean, clearCachedValue: Boolean) {
        if (clearCachedValue) processNewTransactions(newTransactions)
        else {
            val currentTransactionItems: ArrayList<TransactionListItem> =
                transactionListItems.value as ArrayList<TransactionListItem>
            mergeListItems(newTransactions, currentTransactionItems, append)
        }
    }

    private fun processNewTransactions(newTransactions: List<Transaction>) {
        val transactionItemsArrayList: MutableList<TransactionListItem> = mutableListOf()
        transactionItemsArrayList.add(TransactionListItem.HeaderView)
        mergeListItems(newTransactions, transactionItemsArrayList, append = false)
    }

    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    fun buildItems(
        newTransactions: List<Transaction>,
        skipFirstHeader: Boolean = false
    ): ArrayList<TransactionListItem> {
        val result = ArrayList<TransactionListItem>()
        var transactionYear = -1
        var transactionMonth = -1
        if (skipFirstHeader) {
            val date = newTransactions.first().createdAt
            transactionMonth = date.monthValue
            transactionYear = date.year
        }

        var currentTransactionYear: Int
        var currentTransactionMonth: Int
        newTransactions.forEach {
            val date = it.createdAt
            currentTransactionMonth = date.monthValue
            currentTransactionYear = date.year
            if (transactionYear != currentTransactionYear || transactionMonth != currentTransactionMonth) {
                result.add(TransactionListItem.SectionHeader(it.createdAt.format(dateFormatter)))
                transactionYear = currentTransactionYear
                transactionMonth = currentTransactionMonth
            }
            result.add(TransactionListItem.TransactionRow(it))
        }
        return result
    }

    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    fun mergeListItems(
        newTransactions: List<Transaction>,
        currentTransactionItems: MutableList<TransactionListItem>,
        append: Boolean
    ) {
        if (append) {
            val lastCurrentTransaction = currentTransactionItems.findLast {
                it.itemType() == TransactionListItem.TRANSACTION_ROW_VIEW_TYPE
            } as TransactionListItem.TransactionRow
            val currentDate = lastCurrentTransaction.transaction.createdAt
            val newTransactionDate = newTransactions.first().createdAt
            val skipFirstHeader = currentDate.monthValue == newTransactionDate.monthValue &&
                    currentDate.year == newTransactionDate.year
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
            if (index != -1) {
                result.addAll(currentTransactionItems.subList(index, currentTransactionItems.size - 1))
            }
        }
        transactionListItems.postValue(result)
    }

    private fun startIapHelper() {
        viewModelScope.launch {
            iapHelper.initProcess()
        }
    }

    fun onAddToGooglePayPressed(activity: FragmentActivity, requestCode: Int) {
        viewModelScope.launch {
            showLoading()
            iapHelper.startInAppProvisioningFlow(activity, requestCode)
            hideLoading()
        }
    }

    fun onReturnedFromAddToGooglePay() {
        startIapHelper()
    }
}
