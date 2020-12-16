package com.aptopayments.sdk.features.auth.inputemail

import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.mobile.data.user.VerificationStatus
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract

internal class InputEmailViewModel(
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    var enableNextButton: MutableLiveData<Boolean> = MutableLiveData(false)
    var verificationData: MutableLiveData<Verification> = MutableLiveData()

    fun startVerificationUseCase(emailInput: String) {
        showLoading()
        AptoPlatform.startEmailVerification(emailInput) {
            it.either(::handleFailure, ::handleVerification)
        }
    }

    fun handleVerification(verification: Verification) {
        hideLoading()
        if (verification.status == VerificationStatus.PENDING) {
            verificationData.postValue(verification)
        }
    }

    fun viewLoaded() {
        analyticsManager.track(Event.AuthInputEmail)
    }
}
