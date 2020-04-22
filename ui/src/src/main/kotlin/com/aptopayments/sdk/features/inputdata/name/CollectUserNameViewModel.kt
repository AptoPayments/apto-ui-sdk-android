package com.aptopayments.sdk.features.inputdata.name

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.user.NameDataPoint
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent

internal class CollectUserNameViewModel(
    initialValue: NameDataPoint?,
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    val name = MutableLiveData<String>()
    val surname = MutableLiveData<String>()
    val nameError: LiveData<Boolean> = Transformations.map(name) { !isValidField(it) }
    val surnameError: LiveData<Boolean> = Transformations.map(surname) { !isValidField(it) }
    val continueEnabled = MediatorLiveData<Boolean>()
    val continueNext = LiveEvent<NameDataPoint>()

    init {
        continueEnabled.postValue(false)
        continueEnabled.addSource(name) {
            continueEnabled.value = isValidField(name.value) && isValidField(surname.value)
        }
        continueEnabled.addSource(surname) {
            continueEnabled.value = isValidField(name.value) && isValidField(surname.value)
        }
        initialValue?.let {
            name.value = initialValue.firstName
            surname.value = initialValue.lastName
        }
    }

    fun viewLoaded() {
        analyticsManager.track(Event.WorkflowUserInputName)
    }

    fun continueClicked() {
        if (continueEnabled.value == true) {
            continueNext.postValue(NameDataPoint(name.value!!, surname.value!!))
        }
    }

    private fun isValidField(value: String?) = value?.length in 1..60
}
