package com.aptopayments.sdk.features.inputdata.birthdate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.user.BirthdateDataPoint
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent
import org.threeten.bp.LocalDate

internal class CollectUserBirthdateViewModel constructor(
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    private val date = MutableLiveData<LocalDate?>(null)
    val continueEnabled = Transformations.map(date) { it != null }
    val continueClicked = LiveEvent<BirthdateDataPoint>()

    fun viewLoaded() {
        analyticsManager.track(Event.WorkflowUserBirthdate)
    }

    fun setLocalDate(date: LocalDate?) {
        this.date.value = date
    }

    fun onContinueClicked() {
        continueClicked.postValue(BirthdateDataPoint(birthdate = date.value!!))
    }
}
