package com.aptopayments.sdk.features.directdeposit.instructions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent

private const val ACCOUNT_LABEL = "account"
private const val ROUTING_LABEL = "routing"

internal class DirectDepositInstructionsViewModel(
    cardId: String,
    aptoPlatform: AptoPlatformProtocol,
    analytics: AnalyticsServiceContract
) : BaseViewModel() {

    private val _uiState = MutableLiveData(UiState())
    val uiState = _uiState as LiveData<UiState>

    val actions = LiveEvent<Actions>()

    init {
        analytics.track(Event.DirectDepositInstructions)

        showLoading()
        aptoPlatform.fetchCard(cardId, false) { result ->
            result.either(::handleFailure) { card ->
                aptoPlatform.fetchCardProduct(card.cardProductID ?: "", false) {
                    it.either(::handleFailure) { cardProduct ->
                        hideLoading()
                        _uiState.postValue(
                            UiState(
                                cardName = cardProduct.name,
                                accountNumber = card.features?.achAccount?.accountDetails?.accountNumber ?: "",
                                routingNumber = card.features?.achAccount?.accountDetails?.routingNumber ?: ""
                            )
                        )
                    }
                }
            }
        }
    }

    fun onAccountNumberCopy() {
        actions.postValue(Actions.CopyToClipboard(ACCOUNT_LABEL, _uiState.value!!.accountNumber))
    }

    fun onRoutingNumberCopy() {
        actions.postValue(Actions.CopyToClipboard(ROUTING_LABEL, _uiState.value!!.routingNumber))
    }

    sealed class Actions {
        class CopyToClipboard(val label: String, val value: String) : Actions()
    }

    data class UiState(
        val cardName: String = "",
        val accountNumber: String = "",
        val routingNumber: String = ""
    )
}
