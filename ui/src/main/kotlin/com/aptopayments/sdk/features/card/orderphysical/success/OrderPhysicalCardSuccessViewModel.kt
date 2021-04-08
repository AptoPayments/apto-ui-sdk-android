package com.aptopayments.sdk.features.card.orderphysical.success

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.CardStyle
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent

internal class OrderPhysicalCardSuccessViewModel(
    cardId: String,
    aptoPlatform: AptoPlatformProtocol,
    private val analyticsManager: AnalyticsServiceContract,
) : BaseViewModel() {

    private val _state = MutableLiveData<State>()
    val state = _state as LiveData<State>

    val action = LiveEvent<Action>()

    init {
        aptoPlatform.fetchCard(cardId, false) { result ->
            result.runIfRight { _state.postValue(State(it.cardStyle, it.cardNetwork)) }
        }
    }

    fun onDone() {
        analyticsManager.track(Event.OrderPhysicalCardDone)
        action.postValue(Action.OrderPhysicalDone)
    }

    data class State(
        val cardStyle: CardStyle? = null,
        val cardNetwork: Card.CardNetwork? = null
    )

    sealed class Action {
        object OrderPhysicalDone : Action()
    }
}
