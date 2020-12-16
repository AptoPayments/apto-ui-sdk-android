package com.aptopayments.sdk.features.card.fundingsources

import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.fundingsources.Balance
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.core.ui.State
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract

internal class FundingSourcesViewModel(
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    var selectedFundingSourceId: MutableLiveData<String> = MutableLiveData()
    var fundingSourceListItems: MutableLiveData<List<FundingSourceListItem>> = MutableLiveData()
    var state: MutableLiveData<State> = MutableLiveData()

    fun viewReady(cardId: String, selectedBalanceId: String?) {
        this.selectedFundingSourceId.postValue(selectedBalanceId)
        fetchData(cardId, refresh = false) {
            backgroundRefresh(cardId = cardId)
        }
    }

    fun fetchData(cardId: String, refresh: Boolean, onComplete: () -> Unit) {
        state.postValue(State.IN_PROGRESS)
        getFundingSources(cardId = cardId, refresh = refresh, page = 0, rows = Int.MAX_VALUE) {
            state.postValue((State.COMPLETED))
            onComplete()
        }
    }

    private fun backgroundRefresh(cardId: String) {
        getFundingSources(cardId, refresh = true, page = 0, rows = Int.MAX_VALUE) {}
    }

    private fun getFundingSources(
        cardId: String,
        refresh: Boolean,
        page: Int,
        rows: Int,
        onComplete: (() -> Unit)? = null
    ) {
        AptoPlatform.fetchCardFundingSources(
            cardId = cardId,
            forceRefresh = refresh,
            page = page,
            rows = rows
        ) { result ->
            result.either(::handleFailure) { fundingSources ->
                if (fundingSources.isEmpty()) {
                    fundingSourceListItems.postValue(arrayListOf())
                } else {
                    fundingSourceListItems.postValue(
                        generateFundingSourceListItems(
                            fundingSources,
                            selectedFundingSourceId.value
                        )
                    )
                }
                onComplete?.invoke()
            }
        }
    }

    fun setCardFundingSource(cardID: String, fundingSourceID: String, onComplete: (succeed: Boolean) -> Unit) {
        state.postValue(State.IN_PROGRESS)
        AptoPlatform.setCardFundingSource(fundingSourceID, cardID) { result ->
            result.either(::handleFailure) {
                selectedFundingSourceId.postValue(fundingSourceID)
                fundingSourceListItems.value?.mapNotNull {
                    when (it) {
                        is FundingSourceListItem.FundingSourceRow -> it.balance
                        else -> null
                    }
                }?.let {
                    fundingSourceListItems.postValue(generateFundingSourceListItems(it, fundingSourceID))
                }
                state.postValue(State.COMPLETED)
            }
            onComplete(result.isRight)
        }
    }

    private fun generateFundingSourceListItems(
        fundingSources: List<Balance>,
        selected: String?
    ): List<FundingSourceListItem> {
        val list: ArrayList<FundingSourceListItem> = arrayListOf()
        fundingSources.toTypedArray().sortWith(
            Comparator { b1, b2 ->
                when {
                    (b1.balance?.amount ?: 0.0) > (b2.balance?.amount ?: 0.0) -> 1
                    (b1.balance?.amount ?: 0.0) == (b2.balance?.amount ?: 0.0) -> 0
                    else -> -1
                }
            }
        )
        fundingSources.forEach { balance ->
            list.add(FundingSourceListItem.FundingSourceRow(balance, balance.id == selected))
        }
        if (!AptoUiSdk.cardOptions.hideFundingSourcesReconnectButton()) {
            list.add(FundingSourceListItem.AddFundingSourceButton)
        }
        return list
    }

    fun viewLoaded() = analyticsManager.track(Event.ManageCardFundingSourceSelector)
}
