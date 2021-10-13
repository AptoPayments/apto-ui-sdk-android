package com.aptopayments.sdk.features.loadfunds.paymentsources

import android.content.SharedPreferences
import com.aptopayments.mobile.data.paymentsources.NewPaymentSource
import com.aptopayments.mobile.data.paymentsources.PaymentSource
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.functional.Either
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private const val ACCEPTED_ONBOARDING = "ADD_CARD_ACCEPTED_ONBOARDING"

internal class PaymentSourcesRepository(
    private val aptoPlatform: AptoPlatformProtocol,
    private val sharedPreferences: SharedPreferences
) {

    private val _selectedPaymentSource = MutableStateFlow<PaymentSource?>(null)
    val selectedPaymentSource = _selectedPaymentSource as StateFlow<PaymentSource?>

    fun selectPaymentSourceLocally(source: PaymentSource) {
        _selectedPaymentSource.value = source
    }

    fun hasAcceptedOnboarding() = sharedPreferences.getBoolean(ACCEPTED_ONBOARDING, false)

    fun acceptedOnboarding() {
        sharedPreferences.edit().putBoolean(ACCEPTED_ONBOARDING, true).apply()
    }

    suspend fun addPaymentSource(paymentSource: NewPaymentSource): Either<Failure, PaymentSource> {
        return addPaymentSourceWrapper(paymentSource).runIfRightSuspending { refreshSelectedPaymentSource() }
    }

    suspend fun refreshSelectedPaymentSource(): Either<Failure, PaymentSource?> {
        return getPaymentSourceListFromBackend().run { updateSelectedPaymentSource(this) }
    }

    suspend fun getPaymentSourceList(updateSelected: Boolean = false): Either<Failure, List<PaymentSource>> {
        return getPaymentSourceListFromBackend()
            .apply {
                if (updateSelected) {
                    updateSelectedPaymentSource(this)
                }
            }
    }

    private suspend fun getPaymentSourceListFromBackend(): Either<Failure, List<PaymentSource>> =
        suspendCoroutine { cont ->
            aptoPlatform.getPaymentSources({ result ->
                cont.resume(result)
            })
        }

    private suspend fun addPaymentSourceWrapper(paymentSource: NewPaymentSource) =
        suspendCoroutine<Either<Failure, PaymentSource>> { cont ->
            aptoPlatform.addPaymentSource(paymentSource) { result ->
                cont.resume(result)
            }
        }

    suspend fun deletePaymentSource(id: String) = // TODO do something else
        suspendCoroutine<Either<Failure, Unit>> { cont ->
            aptoPlatform.deletePaymentSource(id) { result ->
                cont.resume(result)
            }
        }

    private fun updateSelectedPaymentSource(listResponse: Either<Failure, List<PaymentSource>>): Either<Failure, PaymentSource?> {
        val source = listResponse.map { list -> list.firstOrNull { it.isPreferred } ?: list.firstOrNull() }
        source.runIfRight {
            _selectedPaymentSource.value = it
        }
        return source
    }
}
