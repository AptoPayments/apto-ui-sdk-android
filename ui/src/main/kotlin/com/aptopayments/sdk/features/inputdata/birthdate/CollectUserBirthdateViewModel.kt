package com.aptopayments.sdk.features.inputdata.birthdate

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.user.BirthdateDataPoint
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit

private const val MIN_YEARS_OLD = 18

internal class CollectUserBirthdateViewModel(
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    private val date = MutableLiveData<LocalDate?>(null)
    val continueEnabled = Transformations.map(date) { it != null }
    val continueClicked = LiveEvent<BirthdateDataPoint>()

    init {
        analyticsManager.track(Event.WorkflowUserBirthdate)
    }

    fun setLocalDate(date: LocalDate?) {
        this.date.value = date
    }

    fun onContinueClicked() {
        date.value?.let {
            if (isUserEighteenOrMore(it)) {
                continueClicked.postValue(BirthdateDataPoint(birthdate = it))
            } else {
                handleFailure(YoungerThanEighteenYO())
            }
        }
    }

    private fun isUserEighteenOrMore(dob: LocalDate) = ChronoUnit.YEARS.between(dob, LocalDate.now()) >= MIN_YEARS_OLD

    class YoungerThanEighteenYO : Failure.FeatureFailure("birthday_collector_error_too_young")
}
