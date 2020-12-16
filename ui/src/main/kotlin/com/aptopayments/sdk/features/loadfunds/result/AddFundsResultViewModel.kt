package com.aptopayments.sdk.features.loadfunds.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.mobile.data.payment.Payment
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.utils.LiveEvent

internal class AddFundsResultViewModel(
    cardId: String,
    payment: Payment,
    mapper: PaymentResultElementMapper,
    private val aptoPlatform: AptoPlatformProtocol
) : BaseViewModel() {

    private var cardHolderAgreement: Content? = null
    private val _resultElement = MutableLiveData<PaymentResultElement>()
    val resultElement = _resultElement as LiveData<PaymentResultElement>
    val action = LiveEvent<Action>()

    init {
        fetchSoftDescriptorAndCHA(cardId, mapper, payment)
    }

    private fun fetchSoftDescriptorAndCHA(
        cardId: String,
        mapper: PaymentResultElementMapper,
        payment: Payment
    ) {
        showLoading()
        aptoPlatform.fetchCard(cardId, false) { cardResult ->
            cardResult.either(
                { handleFailure(it) },
                { card ->
                    _resultElement.value = mapper.map(payment, card.features?.funding?.softDescriptor)
                    hideLoading()
                    fetchAgreement(card)
                }
            )
        }
    }

    fun onAgreementClicked() {
        cardHolderAgreement?.let {
            action.value = Action.Agreement(it)
        }
    }

    fun onDoneClicked() {
        action.value = Action.Done
    }

    private fun fetchAgreement(card: Card) {
        card.cardProductID?.let { id ->
            aptoPlatform.fetchCardProduct(id, false) { cardProductResult ->
                cardProductResult.either(
                    { handleFailure(it) },
                    { cardProduct ->
                        this.cardHolderAgreement = cardProduct.cardholderAgreement
                    }
                )
            }
        }
    }

    sealed class Action {
        object Done : Action()
        class Agreement(val content: Content) : Action()
    }
}
