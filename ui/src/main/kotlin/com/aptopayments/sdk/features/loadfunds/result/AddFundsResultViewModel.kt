package com.aptopayments.sdk.features.loadfunds.result

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.data.cardproduct.CardProduct
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

    private var cardProduct: CardProduct? = null
    private val _resultElement = MutableLiveData<PaymentResultElement>()
    val resultElement = _resultElement as LiveData<PaymentResultElement>
    val onAgreement = LiveEvent<Content>()
    val onDone = LiveEvent<Boolean>()

    init {
        showLoading()
        aptoPlatform.fetchFinancialAccount(cardId, false) { cardResult ->
            cardResult.either({ handleFailure(it) }, { card ->
                _resultElement.value = mapper.map(payment, card.features?.funding?.softDescriptor)
                card.cardProductID?.let { id ->
                    aptoPlatform.fetchCardProduct(id, false) { cardProductResult ->
                        cardProductResult.either({ handleFailure(it) }, { cardProduct ->
                            hideLoading()
                            this.cardProduct = cardProduct
                        })
                    }
                }
            })
        }
    }

    fun onAgreementClicked() {
        cardProduct?.let {
            onAgreement.value = it.cardholderAgreement
        }
    }

    fun onDoneClicked() {
        onDone.value = true
    }
}
