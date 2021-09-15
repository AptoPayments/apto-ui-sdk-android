package com.aptopayments.sdk.features.loadfunds.add

import androidx.lifecycle.*
import com.aptopayments.mobile.data.card.FundingLimits
import com.aptopayments.mobile.data.card.Money
import com.aptopayments.mobile.data.payment.Payment
import com.aptopayments.mobile.data.payment.PaymentStatus
import com.aptopayments.mobile.data.paymentsources.PaymentSource
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.extension.localized
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourceElement
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourceElementMapper
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourcesRepository
import com.aptopayments.sdk.utils.LiveEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

private const val CURRENCY = "USD"
private const val CURRENCT_SYMBOL = "$"

internal class AddFundsViewModel(
    private val cardId: String,
    private val repo: PaymentSourcesRepository,
    private val aptoPlatform: AptoPlatformProtocol,
    private val elementMapper: PaymentSourceElementMapper
) : BaseViewModel() {

    private var balanceId = ""
    private var fundingLimits: FundingLimits? = null

    val amount = MutableLiveData("")

    private val _state = MediatorLiveData<State>()
    val state = _state as LiveData<State>

    val action = LiveEvent<Actions>()

    init {
        _state.value = State()
        fetchPreRequisites()
        observeContinueSources()
        viewModelScope.launch {
            repo.selectedPaymentSource.collect { paymentSource ->
                updateState(
                    paymentSource = paymentSource,
                    nullPaymentSource = paymentSource == null,
                )
            }
        }
    }

    @Synchronized
    private fun updateState(
        paymentSource: PaymentSource? = null,
        nullPaymentSource: Boolean? = null,
        amountChanged: Boolean? = null,
    ) {
        _state.value = _state.value?.let { state ->
            state.copy(
                paymentSourceCTAKey = if (paymentSource != null || nullPaymentSource == true) getPaymentSourceCTA(
                    paymentSource
                ) else state.paymentSourceCTAKey,
                paymentSource = paymentSource?.let { getPaymentSourceElement(paymentSource) } ?: state.paymentSource,
                amountError = amountChanged?.let { getAmountError() } ?: state.amountError,
                continueEnabled = canContinueBeEnabled()
            )
        } ?: State()
    }

    private fun fetchSelected() {
        viewModelScope.launch {
            showLoading()
            val selected = repo.refreshSelectedPaymentSource()
            selected.either({ handleFailure(it) }) {
                if (it == null) {
                    action.postValue(Actions.AddPaymentSource)
                }
            }
            hideLoading()
        }
    }

    fun onPaymentSourceClicked() {
        if (repo.selectedPaymentSource.value != null) {
            action.value = Actions.PaymentSourcesList
        } else {
            action.value = Actions.AddPaymentSource
        }
    }

    fun onContinueClicked() {
        showLoading()
        val source = repo.selectedPaymentSource.value!!
        aptoPlatform.pushFunds(
            balanceId,
            source.id,
            Money(CURRENCY, getAmount()!!.toDouble())
        ) { result ->
            hideLoading()
            processLoadFundsResult(result)
        }
    }

    private fun processLoadFundsResult(result: Either<Failure, Payment>) {
        result.either(
            { unableToLoadFunds(it) },
            { payment ->
                if (payment.status == PaymentStatus.FAILED) {
                    unableToLoadFunds()
                } else {
                    action.value = Actions.PaymentResult(payment)
                    updateSelectedPaymentSource()
                }
            }
        )
    }

    private fun updateSelectedPaymentSource() {
        viewModelScope.launch {
            repo.refreshSelectedPaymentSource()
        }
    }

    private fun unableToLoadFunds(failure: Failure? = null) {
        handleFailure(UnableToLoadFundsError(getFailureErrorKey(failure)))
    }

    private fun getFailureErrorKey(failure: Failure?): String {
        return if (failure is Failure.ServerError && !failure.hasUndefinedKey()) {
            failure.errorKey
        } else {
            "load_funds_add_money_error_message"
        }
    }

    private fun getAmount() = amount.value?.toFloatOrNull()

    private fun getPaymentSourceElement(source: PaymentSource?): PaymentSourceElement {
        return source?.let { elementMapper.map(it) } ?: PaymentSourceElement.unsetElement()
    }

    private fun fetchPreRequisites() {
        fetchSelected()
        getFundingLimits()
        getBalanceId()
    }

    private fun getBalanceId() {
        aptoPlatform.fetchCardFundingSource(cardId, false) { result ->
            result.either(
                { handleFailure(it) },
                {
                    balanceId = it.id
                }
            )
        }
    }

    private fun getFundingLimits() {
        aptoPlatform.fetchCard(cardId, false) { result ->
            result.either(
                { handleFailure(it) },
                {
                    fundingLimits = it.features?.funding?.limits
                }
            )
        }
    }

    private fun observeContinueSources() {
        _state.addSource(amount) { updateState(amountChanged = true) }
    }

    private fun canContinueBeEnabled() = checkAmountInsideLimits() && isPaymentSourceCorrect()

    private fun checkAmountInsideLimits() = isAmountCorrect() && getAmount()!!.toFloat() <= getDailyLimit()

    private fun getAmountError(): String {
        return if (isAmountCorrect() && getAmount()!!.toFloat() > getDailyLimit()) {
            "load_funds_add_money_daily_max_title".localized().replace("<<MAX>>", getDailyLimit().toString())
        } else {
            ""
        }
    }

    private fun getDailyLimit() = fundingLimits?.daily?.max?.amount ?: 0.0

    private fun isPaymentSourceCorrect() = _state.value?.paymentSource != null

    private fun isAmountCorrect() = getAmount()?.let { it > 0 } ?: false

    private fun getPaymentSourceCTA(paymentSource: PaymentSource?): String {
        return if (paymentSource == null) {
            "load_funds_add_money_add_card"
        } else {
            "load_funds_add_money_change_card"
        }
    }

    data class State(
        val paymentSourceCTAKey: String = "",
        val paymentSource: PaymentSourceElement? = PaymentSourceElement.unsetElement(),
        val amountError: String = "",
        val continueEnabled: Boolean = false,
        val currencySymbol: String = CURRENCT_SYMBOL
    )

    class UnableToLoadFundsError(key: String) : Failure.FeatureFailure(
        errorKey = key,
        titleKey = "load_funds_add_money_error_title"
    )

    sealed class Actions {
        class PaymentResult(val payment: Payment) : Actions()
        object PaymentSourcesList : Actions()
        object AddPaymentSource : Actions()
    }
}
