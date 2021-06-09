package com.aptopayments.sdk.features.card.orderphysical.initial

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.sdk.features.analytics.Event
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.CardStyle
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.exception.server.ErrorInsufficientFunds
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent

internal class OrderPhysicalCardViewModel(
    private val cardId: String,
    private val aptoPlatform: AptoPlatformProtocol,
    private val analyticsManager: AnalyticsServiceContract,
) : BaseViewModel() {

    private val _state = MutableLiveData<State>()
    val state = _state as LiveData<State>

    val action = LiveEvent<Action>()

    init {
        getConfiguration()
        analyticsManager.track(Event.OrderPhysicalCardStart)
    }

    private fun getConfiguration() {
        showLoading()
        aptoPlatform.getOrderPhysicalCardConfig(cardId) { result ->
            hideLoading()
            result.either(
                {
                    handleFailure(it)
                    action.postValue(Action.NavigateToPreviousScreen)
                },
                {
                    _state.postValue(
                        State(
                            fee = if (it.issuanceFee.amount != 0.0) it.issuanceFee.toString() else "",
                            visibleFee = it.issuanceFee.amount != 0.0
                        )
                    )
                    updateCard()
                }
            )
        }
    }

    private fun updateCard() {
        aptoPlatform.fetchCard(cardId, false) { cardResult ->
            cardResult.runIfRight { card ->
                _state.postValue(
                    state.value!!.copy(
                        cardStyle = card.cardStyle,
                        cardNetwork = card.cardNetwork
                    )
                )
            }
        }
    }

    fun orderCard() {
        showLoading()
        analyticsManager.track(Event.OrderPhysicalCardRequested)
        aptoPlatform.orderPhysicalCard(cardId) { result ->
            result.either(
                {
                    analyticsManager.track(getErrorToTrack(it))
                    handleFailure(it)
                },
                {
                    aptoPlatform.fetchCard(cardId, forceRefresh = true) {
                        hideLoading()
                        action.postValue(Action.ShowSuccessScreen)
                    }
                }
            )
        }
    }

    private fun getErrorToTrack(it: Failure): Event {
        return if (it is ErrorInsufficientFunds) {
            Event.OrderPhysicalCardInsufficientFunds
        } else {
            Event.OrderPhysicalCardError
        }
    }

    fun navigateBack() {
        analyticsManager.track(Event.OrderPhysicalCardDiscarded)
        action.postValue(Action.NavigateToPreviousScreen)
    }

    data class State(
        val fee: String = "",
        val visibleFee: Boolean = false,
        val cardStyle: CardStyle? = null,
        val cardNetwork: Card.CardNetwork? = null
    )

    sealed class Action {
        object ShowSuccessScreen : Action()
        object NavigateToPreviousScreen : Action()
    }
}
