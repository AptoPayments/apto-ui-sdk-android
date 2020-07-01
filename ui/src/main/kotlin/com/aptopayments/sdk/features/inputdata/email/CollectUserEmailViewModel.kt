package com.aptopayments.sdk.features.inputdata.email

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.user.EmailDataPoint
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent
import com.aptopayments.sdk.utils.StringUtils

internal class CollectUserEmailViewModel(
    private val initialValue: EmailDataPoint?,
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    val email = MutableLiveData("")

    val continueEnabled = Transformations.map(email) { StringUtils.isValidEmail(it) }

    val continueNext = LiveEvent<EmailDataPoint>()

    init {
        initialValue?.let {
            email.postValue(initialValue.email)
        }
    }

    fun viewLoaded() {
        analyticsManager.track(Event.WorkflowUserInputEmail)
    }

    fun continueClicked() {
        if (continueEnabled.value == true) {
            continueNext.postValue(EmailDataPoint(email.value!!))
        }
    }
}
