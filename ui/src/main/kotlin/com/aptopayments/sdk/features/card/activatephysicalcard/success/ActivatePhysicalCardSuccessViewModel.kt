package com.aptopayments.sdk.features.card.activatephysicalcard.success

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.PhoneNumber
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.PhoneDialer

internal class ActivatePhysicalCardSuccessViewModel(
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel(), PhoneDialer.Delegate {

    var getPINFinished: MutableLiveData<Boolean> = MutableLiveData()
    private var getPINStarted: Boolean = false
    private var phoneDialer: PhoneDialer? = null

    init {
        analyticsManager.track(Event.ManageCardGetPinNue)
    }

    fun viewResumed() {
        if (getPINStarted) getPINFinished.postValue(true)
    }

    fun getPinTapped(from: Context, phoneNumber: PhoneNumber?) {
        phoneNumber?.let {
            val phoneDialer = PhoneDialer(from)
            this.phoneDialer = phoneDialer
            phoneDialer.dialPhone(it.toStringRepresentation(), this)
            getPINStarted = true
        }
    }

    override fun onTelephonyNotAvailable() {
        handleFailure(NoTelephonyError())
    }

    override fun onCallStarted() {
        getPINStarted = true
    }

    override fun onCallEnded() {
        getPINStarted = false
        getPINFinished.postValue(true)
    }

    override fun onCallCancelled() {
        getPINStarted = false
    }
}

internal class NoTelephonyError : Failure.FeatureFailure()
