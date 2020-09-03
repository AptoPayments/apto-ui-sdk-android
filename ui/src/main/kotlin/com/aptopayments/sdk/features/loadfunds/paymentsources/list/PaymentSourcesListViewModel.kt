package com.aptopayments.sdk.features.loadfunds.paymentsources.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.data.paymentsources.PaymentSource
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourceElement
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourceElementMapper
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourcesRepository
import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.CardNetwork
import com.aptopayments.sdk.utils.LiveEvent

internal class PaymentSourcesListViewModel(
    private val repo: PaymentSourcesRepository,
    private val elementMapper: PaymentSourceElementMapper
) : BaseViewModel() {

    val sourceSelected = LiveEvent<Boolean>()
    val newPaymentSource = LiveEvent<Boolean>()

    private val _sourceList = MutableLiveData<List<PaymentSourcesListItem>>(emptyList())
    val sourceList = _sourceList as LiveData<List<PaymentSourcesListItem>>

    fun onPresented() {
        loadPaymentSourcesList()
    }

    private fun loadPaymentSourcesList() {
        showLoading()
        repo.getPaymentSourcesList(forceApiCall = false) { result ->
            hideLoading()
            result.either({ handleFailure(it) }, { list ->
                _sourceList.postValue(mapPaymentSources(list, repo.selectedPaymentSource.value))
            })
        }
    }

    fun selectPaymentSource(id: String) {
        repo.selectPaymentSource(id)
        sourceSelected.postValue(true)
    }

    fun newPaymentSource() {
        newPaymentSource.postValue(true)
    }

    private fun mapPaymentSources(
        list: List<PaymentSource>,
        preferred: PaymentSource?
    ): MutableList<PaymentSourcesListItem> {
        return list.map {
            PaymentSourcesListItem(
                elementMapper.map(it),
                it.id == preferred?.id,
                PaymentSourcesListItem.Type.EXISTING
            )
        }.toMutableList().apply { add(getNewPaymentMethodItem()) }
    }

    private fun getNewPaymentMethodItem() =
        PaymentSourcesListItem(
            PaymentSourceElement(
                id = "",
                title = "load_funds_payment_methods_new_card_element_title".localized(),
                subtitle = "load_funds_payment_methods_new_card_element_subtitle".localized(),
                showFourDots = false,
                logo = CardNetwork.UNKNOWN.icon
            ), false, PaymentSourcesListItem.Type.NEW
        )
}
