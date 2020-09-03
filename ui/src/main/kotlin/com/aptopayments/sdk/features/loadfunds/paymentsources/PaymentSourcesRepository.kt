package com.aptopayments.sdk.features.loadfunds.paymentsources

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.data.paymentsources.Card
import com.aptopayments.mobile.data.paymentsources.NewPaymentSource
import com.aptopayments.mobile.data.paymentsources.PaymentSource
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.functional.right
import com.aptopayments.mobile.platform.AptoPlatformProtocol

private const val ACCEPTED_ONBOARDING = "ADD_CARD_ACCEPTED_ONBOARDING"

internal class PaymentSourcesRepository(
    private val aptoPlatform: AptoPlatformProtocol,
    private val sharedPreferences: SharedPreferences
) {

    private var cachedPaymentSources: MutableList<PaymentSource> = mutableListOf()

    private val _selectedPaymentSource: MutableLiveData<PaymentSource?> = MutableLiveData(null)
    val selectedPaymentSource = _selectedPaymentSource as LiveData<PaymentSource?>

    fun addPaymentSource(paymentSource: NewPaymentSource, callback: (Either<Failure, PaymentSource>) -> Unit) {
        aptoPlatform.addPaymentSource(paymentSource) { result ->
            result.runIfRight {
                if (cachedPaymentSources.isEmpty() || it.isPreferred) {
                    _selectedPaymentSource.postValue(it)
                }
                cachedPaymentSources.add(it)
            }
            callback(result)
        }
    }

    fun selectPaymentSource(id: String) {
        getPaymentSourceFromCache(id)?.let {
            _selectedPaymentSource.postValue(it)
        }
    }

    fun getSelectedPaymentSourceLiveData(): LiveData<PaymentSource?> {
        if (_selectedPaymentSource.value == null) {
            getPaymentSourcesFromAPI {}
        }
        return _selectedPaymentSource
    }

    private fun getPaymentSourceFromCache(id: String) = cachedPaymentSources.firstOrNull { it.id == id }

    fun getPaymentSourcesList(forceApiCall: Boolean, callback: (Either<Failure, List<PaymentSource>>) -> Unit) {
        if (forceApiCall || cachedPaymentSources.isEmpty()) {
            getPaymentSourcesFromAPI(callback)
        } else {
            callback(cachedPaymentSources.right())
        }
    }

    private fun getPaymentSourcesFromAPI(callback: (Either<Failure, List<PaymentSource>>) -> Unit) {
        aptoPlatform.getPaymentSources({ result ->
            result.runIfRight {
                setValueIfFirstTime(it)
                cachedPaymentSources = it.filterIsInstance<Card>().toMutableList()
            }
            callback(result)
        })
    }

    private fun setValueIfFirstTime(list: List<PaymentSource>) {
        if (_selectedPaymentSource.value == null) {
            _selectedPaymentSource.value = getSelectedPaymentSourceFromList(list)
        }
    }

    private fun getSelectedPaymentSourceFromList(list: List<PaymentSource>): PaymentSource? {
        return if (list.isNotEmpty()) {
            list.firstOrNull { it.isPreferred } ?: list.first()
        } else {
            null
        }
    }

    fun hasAcceptedOnboarding() = sharedPreferences.getBoolean(ACCEPTED_ONBOARDING, false)

    fun acceptedOnboarding() {
        sharedPreferences.edit().putBoolean(ACCEPTED_ONBOARDING, true).apply()
    }
}
