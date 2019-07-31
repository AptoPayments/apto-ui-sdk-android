package com.aptopayments.sdk.features.auth.inputphone

import androidx.lifecycle.MutableLiveData
import com.aptopayments.core.data.PhoneNumber
import com.aptopayments.core.data.user.Verification
import com.aptopayments.core.data.user.VerificationStatus
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.core.ui.State
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.core.analytics.Event
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

internal class InputPhoneViewModel
@Inject constructor(
        private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel(), CoroutineScope {
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default

    var enableNextButton: MutableLiveData<Boolean> = MutableLiveData()
    var state:  MutableLiveData<State> = MutableLiveData()
    var verificationData: MutableLiveData<Verification> = MutableLiveData()
    lateinit var phoneNumber: PhoneNumber
    private val phoneNumberUtil = PhoneNumberUtil.getInstance()

    fun startVerificationUseCase(phoneNumberInput: String, countryCode: String) = launch {
        state.postValue(State.IN_PROGRESS)
        val parsedPhoneNumber = phoneNumberUtil.parse(phoneNumberInput, countryCode)
        phoneNumber = PhoneNumber(parsedPhoneNumber.countryCode.toString(), parsedPhoneNumber.nationalNumber.toString())
        AptoPlatform.startPhoneVerification(phoneNumber) {
            it.either(::handleFailure, ::handleVerification)
        }
    }

    fun handleVerification(verification: Verification) {
        state.postValue(State.COMPLETED)
        if (verification.status == VerificationStatus.PENDING) {
            verification.verificationDataPoint = phoneNumber.toStringRepresentation()
            verificationData.postValue(verification)
        }
    }

    fun viewLoaded() = analyticsManager.track(Event.AuthInputPhone)
}
