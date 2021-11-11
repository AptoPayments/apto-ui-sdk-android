package com.aptopayments.sdk.features.p2p.funds

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.data.card.Money
import com.aptopayments.mobile.data.fundingsources.Balance
import com.aptopayments.mobile.data.payment.PaymentStatus
import com.aptopayments.mobile.data.transfermoney.CardHolderData
import com.aptopayments.mobile.data.transfermoney.P2pTransferResponse
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourceElement
import com.aptopayments.sdk.utils.LiveEvent

private const val DEFAULT_CURRENCY = "USD"
private const val DEFAULT_CURRENCY_SYMBOL = "$"

internal class SendFundsViewModel(
    cardId: String,
    private val recipient: CardHolderData,
    private val aptoPlatform: AptoPlatformProtocol
) : BaseViewModel() {
    val amount = MutableLiveData("")

    private val _state = MediatorLiveData<State>()
    val state = _state as LiveData<State>

    val action = LiveEvent<Action>()

    private var balance = Balance(amountSpendable = Money(DEFAULT_CURRENCY, 0.0))

    init {
        _state.value = State(
            ctaEnabled = false,
            recipient = PaymentSourceElement.genericElement(recipient.name.toString())
        )
        fetchPreRequisites(cardId)

        _state.addSource(amount) { amount -> amountChanged(amount) }
    }

    private fun fetchPreRequisites(cardId: String) {
        showLoading()
        aptoPlatform.fetchCardFundingSource(cardId, false) { result ->
            result.either(
                { handleFailure(it) },
                {
                    hideLoading()
                    balance = it
                    _state.value = _state.value!!.copy(
                        currencySymbol = it.getSpendable().currencySymbol(),
                        maxSpendable = it.getSpendable().amount.toString()
                    )
                }
            )
        }
    }

    private fun amountChanged(amount: String) {
        val amountParsed = amount.toDoubleOrNull() ?: 0.0
        val maxAllowed = balance.getSpendable().amount ?: 0.0

        val continueEnabled: Boolean
        val error: Boolean

        when {
            amountParsed == 0.0 -> {
                continueEnabled = false
                error = false
            }
            amountParsed > maxAllowed -> {
                continueEnabled = false
                error = true
            }
            else -> {
                continueEnabled = true
                error = false
            }
        }

        _state.value = _state.value!!.copy(
            ctaEnabled = continueEnabled,
            amountError = error
        )
    }

    fun onCtaClicked() {
        if (state.value?.ctaEnabled == true) {
            showLoading()
            aptoPlatform.p2pMakeTransfer(
                balance.id,
                recipient.cardholderId,
                Money(DEFAULT_CURRENCY, amount = amount.value!!.toDoubleOrNull())
            ) { result ->
                result.either(
                    { handleFailure(it) },
                    {
                        hideLoading()
                        if (it.status != PaymentStatus.FAILED) {
                            action.postValue(Action.PaymentSuccess(it))
                        } else {
                            action.postValue(Action.PaymentFailure)
                        }
                    }
                )
            }
        }
    }

    fun onChangeRecipient() {
        action.postValue(Action.ChangeRecipient)
    }

    data class State(
        val amountError: Boolean = false,
        val ctaEnabled: Boolean = false,
        val currencySymbol: String = DEFAULT_CURRENCY_SYMBOL,
        val maxSpendable: String = "",
        val recipient: PaymentSourceElement = PaymentSourceElement.genericElement(""),
    )

    sealed class Action {
        class PaymentSuccess(val payment: P2pTransferResponse) : Action()
        object PaymentFailure : Action()
        object ChangeRecipient : Action()
    }
}
