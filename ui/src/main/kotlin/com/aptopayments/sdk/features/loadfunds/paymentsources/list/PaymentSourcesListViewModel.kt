package com.aptopayments.sdk.features.loadfunds.paymentsources.list

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aptopayments.mobile.data.paymentsources.PaymentSource
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourceElement
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourceElementMapper
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourcesRepository
import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.CardNetwork
import com.aptopayments.sdk.utils.CoroutineDispatcherProvider
import com.aptopayments.sdk.utils.LiveEvent
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class PaymentSourcesListViewModel(
    private val repo: PaymentSourcesRepository,
    private val elementMapper: PaymentSourceElementMapper,
    private val dispatchers: CoroutineDispatcherProvider
) : BaseViewModel() {

    private val _sourceList = MutableLiveData<List<PaymentSourcesListItem>>(emptyList())
    val sourceList = _sourceList as LiveData<List<PaymentSourcesListItem>>
    val actions = LiveEvent<Actions>()

    fun onPresented() {
        loadPaymentSourcesList()
    }

    private fun loadPaymentSourcesList() {
        showLoading()
        viewModelScope.launch(dispatchers.io) {
            repo.getPaymentSourceList().either(::handleFailure) {
                _sourceList.postValue(mapPaymentSources(it, repo.selectedPaymentSource.value))
            }
            withContext(dispatchers.main) {
                hideLoading()
            }
        }
    }

    fun selectPaymentSource(source: PaymentSource) {
        repo.selectPaymentSourceLocally(source)
        actions.postValue(Actions.SourceSelected)
    }

    fun newPaymentSource() {
        actions.postValue(Actions.NewPaymentSource)
    }

    private fun mapPaymentSources(
        list: List<PaymentSource>,
        preferred: PaymentSource?
    ): MutableList<PaymentSourcesListItem> {
        return list.map {
            createPaymentSourceListItem(it, preferred)
        }.toMutableList().apply { add(getNewPaymentMethodItem()) }
    }

    private fun createPaymentSourceListItem(
        it: PaymentSource,
        preferred: PaymentSource?
    ): PaymentSourcesListItem {
        return PaymentSourcesListItem(
            elementMapper.map(it),
            isPreferred(it, preferred),
            PaymentSourcesListItem.Type.EXISTING,
            it
        )
    }

    private fun isPreferred(current: PaymentSource, preferred: PaymentSource?): Boolean {
        return if (preferred != null) {
            current.id == preferred.id
        } else {
            current.isPreferred
        }
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

    sealed class Actions {
        object SourceSelected : Actions()
        object NewPaymentSource : Actions()
    }
}
