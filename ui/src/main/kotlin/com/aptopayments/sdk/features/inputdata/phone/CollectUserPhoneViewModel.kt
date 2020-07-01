package com.aptopayments.sdk.features.inputdata.phone

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.PhoneNumber
import com.aptopayments.mobile.data.user.PhoneDataPoint
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent
import com.google.i18n.phonenumbers.PhoneNumberUtil

internal class CollectUserPhoneViewModel(private val analyticsManager: AnalyticsServiceContract) : BaseViewModel() {

    private var phoneNumber = ""
    private var countryCode = ""
    private val _continueEnabled = MutableLiveData(false)
    val continueEnabled = _continueEnabled as LiveData<Boolean>
    val continuePressed = LiveEvent<PhoneDataPoint>()

    fun viewLoaded() {
        analyticsManager.track(Event.WorkflowUserPhone)
    }

    fun onContinueClicked() {
        showLoading()
        val parsedPhoneNumber = PhoneNumberUtil.getInstance().parse(phoneNumber, countryCode)
        val phoneNumber =
            PhoneNumber(parsedPhoneNumber.countryCode.toString(), parsedPhoneNumber.nationalNumber.toString())
        continuePressed.postValue(PhoneDataPoint(phoneNumber))
    }

    fun onPhoneChanged(phoneNumber: String, valid: Boolean) {
        this.phoneNumber = phoneNumber
        _continueEnabled.value = valid
    }

    fun onCountryChanged(countryCode: String) {
        this.countryCode = countryCode
    }
}
