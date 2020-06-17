package com.aptopayments.sdk.features.auth.inputphone

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.PhoneNumber
import com.aptopayments.core.data.user.Verification
import com.aptopayments.core.data.user.VerificationStatus
import com.aptopayments.core.features.managecard.CardOptions
import com.aptopayments.core.platform.AptoPlatformProtocol
import com.aptopayments.sdk.core.platform.AptoUiSdkProtocol
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.google.i18n.phonenumbers.PhoneNumberUtil

internal class InputPhoneViewModel constructor(
    private val analyticsManager: AnalyticsServiceContract,
    private val aptoPlatform: AptoPlatformProtocol,
    private val aptoUiSdkProtocol: AptoUiSdkProtocol
) : BaseViewModel() {

    private var phoneNumber = ""
    private var countryCode = ""
    private val _enableNextButton = MutableLiveData(false)
    val enableNextButton = _enableNextButton as LiveData<Boolean>
    val verificationData = MutableLiveData<Verification?>(null)
    val showXOnToolbar: Boolean by lazy { isSdkEmbedded() }

    fun onContinueClicked() {
        showLoading()
        val parsedPhoneNumber = PhoneNumberUtil.getInstance().parse(phoneNumber, countryCode)
        val phoneNumber =
            PhoneNumber(parsedPhoneNumber.countryCode.toString(), parsedPhoneNumber.nationalNumber.toString())
        aptoPlatform.startPhoneVerification(phoneNumber) { result ->
            result.either(::handleFailure) { handleVerification(it, phoneNumber) }
        }
    }

    private fun handleVerification(verification: Verification, phoneNumber: PhoneNumber) {
        hideLoading()
        if (verification.status == VerificationStatus.PENDING) {
            verification.verificationDataPoint = phoneNumber.toStringRepresentation()
            verificationData.postValue(verification)
        }
    }

    fun viewLoaded() = analyticsManager.track(Event.AuthInputPhone)

    fun onPhoneChanged(phoneNumber: String, valid: Boolean) {
        this.phoneNumber = phoneNumber
        _enableNextButton.value = valid
    }

    fun onCountryChanged(countryCode: String) {
        this.countryCode = countryCode
    }

    private fun isSdkEmbedded() = aptoUiSdkProtocol.cardOptions.openingMode == CardOptions.OpeningMode.EMBEDDED
}
