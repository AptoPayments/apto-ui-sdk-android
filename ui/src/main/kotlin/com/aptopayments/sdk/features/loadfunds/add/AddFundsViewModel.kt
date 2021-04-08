package com.aptopayments.sdk.features.loadfunds.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aptopayments.mobile.data.card.FundingLimits
import com.aptopayments.mobile.data.card.Money
import com.aptopayments.mobile.data.payment.Payment
import com.aptopayments.mobile.data.payment.PaymentStatus
import com.aptopayments.mobile.data.paymentsources.PaymentSource
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.extension.localized
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.extension.toOnlyDigits
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourceElement
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourceElementMapper
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourcesRepository
import com.aptopayments.sdk.utils.LiveEvent
import com.aptopayments.sdk.utils.extensions.map
import kotlinx.coroutines.launch

private const val CURRENCY = "USD"

internal class AddFundsViewModel(
    private val cardId: String,
    private val repo: PaymentSourcesRepository,
    private val aptoPlatform: AptoPlatformProtocol,
    private val elementMapper: PaymentSourceElementMapper
) : BaseViewModel() {

    private var balanceId = ""
    private var fundingLimits: FundingLimits? = null

    private val selectedPaymentSource = repo.selectedPaymentSource
    val amount = MutableLiveData("$")
    val paymentSource = selectedPaymentSource.map { getPaymentSourceElement(it) }
    val error = MutableLiveData("")

    val paymentSourceCTA = selectedPaymentSource.map { getPaymentSourceCTA(it) }

    private val _continueEnabled = MediatorLiveData<Boolean>()
    val continueButtonEnabled = _continueEnabled as LiveData<Boolean>

    val action = LiveEvent<Actions>()

    init {
        fetchPreRequisites()
        observeContinueSources()
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
        if (selectedPaymentSource.value != null) {
            action.value = Actions.PaymentSourcesList
        } else {
            action.value = Actions.AddPaymentSource
        }
    }

    fun onContinueClicked() {
        showLoading()
        val source = paymentSource.value!!
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

    private fun getAmount() = amount.value?.toOnlyDigits()

    private fun getPaymentSourceElement(source: PaymentSource?): PaymentSourceElement {
        return source?.let { elementMapper.map(it) } ?: elementMapper.getUnsetElement()
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
        _continueEnabled.addSource(paymentSource) { onContinueFieldChanged() }
        _continueEnabled.addSource(amount) { onContinueFieldChanged() }
    }

    private fun onContinueFieldChanged() {
        _continueEnabled.postValue(checkAllFieldCorrectness())
    }

    private fun checkAllFieldCorrectness(): Boolean {
        return checkAmountInsideLimits() && isPaymentSourceCorrect()
    }

    private fun checkAmountInsideLimits(): Boolean {
        return if (isAmountCorrect()) {
            val requested = getAmount()!!.toFloat()
            when {
                requested > getDailyLimit() -> {
                    postError("load_funds_add_money_daily_max_title".localized(), getDailyLimit())
                    false
                }
                else -> {
                    postError("")
                    true
                }
            }
        } else {
            postError("")
            false
        }
    }

    private fun postError(legend: String, limit: Double = 0.0) {
        error.value = legend.replace("<<MAX>>", limit.toInt().toString())
    }

    private fun getDailyLimit() = fundingLimits?.daily?.max?.amount ?: 0.0

    private fun isPaymentSourceCorrect() = paymentSource.value?.isDefined() ?: false

    private fun isAmountCorrect() = !getAmount().isNullOrEmpty() && (getAmount()!!.toDouble() > 0)

    private fun getPaymentSourceCTA(paymentSource: PaymentSource?): String {
        return if (paymentSource == null) {
            "load_funds_add_money_add_card"
        } else {
            "load_funds_add_money_change_card"
        }.localized()
    }

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
