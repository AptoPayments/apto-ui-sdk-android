package com.aptopayments.sdk.features.managecard

import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.fundingsources.Balance
import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.mobile.features.managecard.CardOptions
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.repository.*
import com.aptopayments.sdk.repository.CardAction
import com.aptopayments.sdk.repository.CardActionRepository
import com.aptopayments.sdk.ui.views.PCIConfiguration
import com.aptopayments.sdk.utils.LiveEvent
import com.aptopayments.sdk.utils.extensions.distinctUntilChanged
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.koin.core.parameter.parametersOf
import java.lang.RuntimeException

private const val ROWS_PER_PAGE = 20

internal class ManageCardViewModel(
    private val cardId: String,
    private val getTransactionsQueue: FetchTransactionsTaskQueue,
    private val analyticsManager: AnalyticsServiceContract,
    private val aptoUiSdkProtocol: AptoUiSdkProtocol,
    private val aptoPlatform: AptoPlatformProtocol
) : BaseViewModel(), KoinComponent {

    private val cardActionRepo: CardActionRepository by inject()
    private val iapHelper: IAPHelper by inject { parametersOf(cardId) }
    private val _card: MutableLiveData<Card> = MutableLiveData()
    val card = _card.distinctUntilChanged()
    val cardConfiguration: PCIConfiguration by lazy { PCIConfigurationBuilder().build(cardId) }

    private val _menuState = MutableLiveData(menuInitialState())
    val menuState = _menuState as LiveData<MenuState>

    val cardAction: LiveEvent<CardAction> = cardActionRepo.event
    val showFundingSourceDialog = LiveEvent<String>()
    val transactions = MutableLiveData(listOf<Transaction>())
    val fundingSource = MutableLiveData<Balance?>()
    var transactionsInfoRetrieved: Boolean = false
    val canBackPress: Boolean by lazy { isSdkEmbedded() }
    val showXOnToolbar: Boolean by lazy { isSdkEmbedded() }

    private var lastTransactionId: String? = null
    var balanceLoaded = false

    val transactionListItems = MediatorLiveData<List<TransactionListItem>>()

    private val iapState = iapHelper.state.asLiveData()
    private val _emptyState = MediatorLiveData<EmptyState>()
    val emptyState = _emptyState as LiveData<EmptyState>

    init {
        analyticsManager.track(Event.ManageCard)
        startIapHelper()
        fetchData(forceApiCall = false, clearCachedValue = false) {
            backgroundRefresh()
        }
        transactionListItems.addSource(transactions) {
            generateTransactionList(it)
        }
        _emptyState.addSource(transactions) { calculateEmptyState() }
        _emptyState.addSource(iapState) { calculateEmptyState() }
    }

    private fun calculateEmptyState() {
        var showAddToGPay = false
        val emptyTransactions = transactions.value.isNullOrEmpty() && transactionsInfoRetrieved

        if (emptyTransactions) {
            showAddToGPay = calcIapEnabled(iapHelper, _card.value)
        }
        _emptyState.postValue(
            EmptyState(
                showContainer = emptyTransactions || showAddToGPay,
                showAddToGPay = showAddToGPay,
                showNoTransactions = emptyTransactions && !showAddToGPay
            )
        )
    }

    private fun calcIapEnabled(iapHelper: IAPHelper, card: Card?) =
        iapHelper.satisfyHardwareRequisites() &&
            iapHelper.state.value is ProvisioningState.CanBeAdded &&
            card?.features?.inAppProvisioning?.isEnabled == true

    private fun menuInitialState(): MenuState {
        return with(aptoUiSdkProtocol.cardOptions) {
            MenuState(
                showStats = showStatsButton(),
                showAccountSettings = showAccountSettingsButton()
            )
        }
    }

    private fun generateTransactionList(transactionItems: List<Transaction>) {
        val calculator = TransactionListCalculatorWithHeader()
        val output = mutableListOf<TransactionListItem>()
        output.add(TransactionListItem.HeaderView)
        output.addAll(calculator.buildList(transactionItems))
        transactionListItems.value = output
    }

    fun refreshData(onComplete: (() -> Unit)) {
        fetchData(forceApiCall = true, clearCachedValue = true, onComplete = onComplete)
    }

    fun refreshTransactions() {
        showLoading()
        getTransactions(forceApiCall = true, clearCachedValue = true) {
            hideLoading()
        }
    }

    private fun fetchData(forceApiCall: Boolean, clearCachedValue: Boolean, onComplete: () -> Unit) {
        transactionsInfoRetrieved = false
        getCard(refresh = forceApiCall) { card ->
            card.cardProductID?.let {
                getCardBalance(refresh = forceApiCall) {
                    getTransactions(forceApiCall = forceApiCall, clearCachedValue = clearCachedValue) {
                        onComplete.invoke()
                    }
                }
            }
        }
    }

    private fun backgroundRefresh() {
        getCard(refresh = true) { }
        getCardBalance(refresh = true) {}
        getBackgroundTransactions()
    }

    fun refreshCard() = getCard(refresh = true) {}

    private fun getCard(refresh: Boolean, onComplete: ((Card) -> Unit)? = null) {
        aptoPlatform.fetchCard(cardId = cardId, forceRefresh = refresh) { result ->
            result.either(::handleFailure) { card ->
                updateViewModelWithCard(card)
                onComplete?.invoke(card)
            }
        }
    }

    private fun updateViewModelWithCard(card: Card) {
        _card.postValue(card)
        _menuState.postValue(
            _menuState.value!!.copy(showPhysicalCardActivationMessage = card.orderedStatus == Card.OrderedStatus.ORDERED)
        )
    }

    fun refreshBalance() {
        getCardBalance(refresh = true) {}
    }

    private fun getCardBalance(refresh: Boolean, onComplete: (() -> Unit)? = null) {
        // TODO: use refresh = false to read from cache
        aptoPlatform.fetchCardFundingSource(cardId, refresh) {
            balanceLoaded = true
            it.either(::handleFailure) { balance -> fundingSource.postValue(balance) }
            onComplete?.invoke()
        }
    }

    private fun getTransactions(
        forceApiCall: Boolean,
        clearCachedValue: Boolean,
        onComplete: () -> Unit
    ) {
        getTransactionsQueue.loadTransactions(cardId, ROWS_PER_PAGE, forceApiCall, clearCachedValue) { result ->
            result.either(::handleFailure) { transactionList ->
                transactionsInfoRetrieved = true
                updateTransactions(
                    transactionList,
                    currentList = transactions.value,
                    append = false,
                    clearCachedValue = true
                )
                onComplete()
            }
        }
    }

    fun getMoreTransactions() {
        getTransactionsQueue.loadMoreTransactions(cardId, lastTransactionId, ROWS_PER_PAGE) { result ->
            result.either(::handleFailure) { transactionList ->
                updateTransactions(transactionList, currentList = transactions.value, append = true)
            }
        }
    }

    private fun getBackgroundTransactions() {
        getTransactionsQueue.backgroundRefresh(cardId, ROWS_PER_PAGE) { result ->
            result.either(::handleFailure) { transactionList ->
                transactionsInfoRetrieved = true
                updateTransactions(transactionList, currentList = transactions.value, append = false)
            }
        }
    }

    private fun updateTransactions(
        newList: List<Transaction>,
        currentList: List<Transaction>?,
        append: Boolean,
        clearCachedValue: Boolean = false
    ) {
        val merger = TransactionListMerger()
        val list = merger.merge(newList, currentList, append, clearCachedValue)

        list.lastOrNull()?.let { lastTransactionId = it.transactionId }
        transactions.postValue(list)
    }

    private fun startIapHelper() {
        viewModelScope.launch {
            iapHelper.initProcess()
        }
    }

    fun onAddToGooglePayPressed(activity: FragmentActivity) {
        viewModelScope.launch {
            showLoading()
            try {
                iapHelper.startInAppProvisioningFlow(activity)
            } catch (e: RuntimeException) {
                handleFailure(UnableToProvisionCard())
            } finally {
                hideLoading()
            }
        }
    }

    fun onActivityResult(requestCode: Int, result: Boolean): Boolean {
        return iapHelper.onActivityResult(requestCode, result, viewModelScope)
    }

    private fun isSdkEmbedded() = aptoUiSdkProtocol.cardOptions.openingMode == CardOptions.OpeningMode.EMBEDDED

    fun onFundingSourceTapped() {
        if (card.value?.features?.selectBalanceStore?.allowedBalanceTypes?.isNotEmpty() == true) {
            showFundingSourceDialog.postValue(fundingSource.value?.id)
        }
    }

    fun onCardTapped() {
        if (fundingSource.value?.state != Balance.BalanceState.VALID) {
            showFundingSourceDialog.postValue(fundingSource.value?.id)
        }
    }

    data class EmptyState(
        val showContainer: Boolean = false,
        val showNoTransactions: Boolean = false,
        val showAddToGPay: Boolean = false,
    )

    data class MenuState(
        val showPhysicalCardActivationMessage: Boolean = false,
        val showStats: Boolean = false,
        val showAccountSettings: Boolean = false,
    )

    override fun onCleared() {
        super.onCleared()
        getTransactionsQueue.cancel()
    }
}
