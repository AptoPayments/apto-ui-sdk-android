package com.aptopayments.sdk.features.loadfunds.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
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

private const val CURRENCY = "USD"

internal class AddFundsViewModel(
    private val cardId: String,
    paymentSourcesRepo: PaymentSourcesRepository,
    private val aptoPlatform: AptoPlatformProtocol,
    private val elementMapper: PaymentSourceElementMapper
) : BaseViewModel() {

    private var balanceId = ""
    private var fundingLimits: FundingLimits? = null

    private val selectedPaymentSource = paymentSourcesRepo.getSelectedPaymentSourceLiveData()
    val amount = MutableLiveData("$")
    val paymentSource = selectedPaymentSource.map { getPaymentSourceElement(it) }
    val error = MutableLiveData("")

    val paymentSourceCTA = selectedPaymentSource.map { getPaymentSourceCTA(it) }

    private val _continueEnabled = MediatorLiveData<Boolean>()
    val continueButtonEnabled = _continueEnabled as LiveData<Boolean>
    val paymentMade = LiveEvent<Payment>()

    val paymentSourceClicked = LiveEvent<Boolean>()

    init {
        fetchPreRequisites()
        observeContinueSources()
    }

    fun onPaymentSourceClicked() {
        paymentSourceClicked.postValue(true)
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
        result.either({ unableToLoadFunds(it) }, { payment ->
            if (payment.status == PaymentStatus.FAILED) {
                unableToLoadFunds()
            } else {
                paymentMade.value = payment
            }
        })
    }

    private fun unableToLoadFunds(failure: Failure? = null) {
        handleFailure(UnableToLoadFundsError(getFailureErrorKey(failure)))
    }

    private fun getFailureErrorKey(failure: Failure?): String {
        return if (failure is Failure.ServerError && !failure.hasUndefinedKey()) {
            failure.getErrorKey()
        } else {
            "load_funds_add_money_error_message"
        }
    }

    private fun getAmount() = amount.value?.toOnlyDigits()

    private fun getPaymentSourceElement(source: PaymentSource?): PaymentSourceElement {
        return source?.let { elementMapper.map(it) } ?: elementMapper.getUnsetElement()
    }

    private fun fetchPreRequisites() {
        showLoading()
        aptoPlatform.fetchFinancialAccount(cardId, false) { result ->
            hideLoading()
            result.either({ handleFailure(it) }, {
                fundingLimits = it.features?.funding?.limits
            })
        }
        aptoPlatform.fetchCardFundingSource(cardId, false) { result ->
            result.either({ handleFailure(it) }, {
                balanceId = it.id
            })
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
        message = key,
        title = "load_funds_add_money_error_title"
    )
}
