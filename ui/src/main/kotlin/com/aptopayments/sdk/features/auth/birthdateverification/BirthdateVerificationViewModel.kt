package com.aptopayments.sdk.features.auth.birthdateverification

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.user.BirthdateDataPoint
import com.aptopayments.mobile.data.user.DataPoint
import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.mobile.data.user.VerificationStatus
import com.aptopayments.mobile.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

private const val AUTH_TYPE = "birthdate"

internal class BirthdateVerificationViewModel(
    private val verification: Verification,
    private val analyticsManager: AnalyticsServiceContract,
    private val aptoPlatform: AptoPlatformProtocol
) : BaseViewModel() {

    private val date = MutableLiveData<LocalDate?>(null)
    val verificationError = LiveEvent<Boolean>()
    val continueEnabled = Transformations.map(date) { it != null }
    val birthdateVerified = LiveEvent<DataPoint>()

    fun viewLoaded() {
        analyticsManager.track(Event.AuthVerifyBirthdate)
    }

    fun setLocalDate(date: LocalDate?) {
        this.date.value = date
    }

    fun onContinueClicked() {
        verification.secondaryCredential?.verificationId?.let { verificationId ->
            showLoading()
            val request = getRequestParams(verificationId, date.value!!)
            aptoPlatform.completeVerification(request) {
                it.either(::handleFailure, ::handleVerification)
            }
        }
    }

    private fun getRequestParams(verificationId: String, date: LocalDate) =
        Verification(
            verificationId = verificationId,
            secret = date.format(DateTimeFormatter.ISO_DATE),
            verificationType = AUTH_TYPE
        )

    private fun handleVerification(response: Verification) {
        hideLoading()
        if (response.status == VerificationStatus.PASSED) {
            birthdateVerified.postValue(BirthdateDataPoint(date.value!!, response))
        } else {
            verificationError.postValue(true)
        }
    }
}
