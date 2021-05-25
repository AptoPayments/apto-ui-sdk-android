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
private const val MAX_YEARS_OLD = 120
private const val FOUR_DIGITS_YEAR = 1000

internal class CollectUserBirthdateViewModel(
    analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    private val date = MutableLiveData<LocalDate?>(null)
    val continueEnabled = Transformations.map(date) { it != null }
    val continueClicked = LiveEvent<BirthdateDataPoint>()

    init {
        analyticsManager.track(Event.WorkflowUserBirthdate)
    }

    fun setLocalDate(date: LocalDate?) {
        if (date != null && date.year > FOUR_DIGITS_YEAR) {
            when {
                isUserMoreThanMaxAge(date) -> setFailure(OlderThanMaxAgeFailure())
                isUserLessThanMinAge(date) -> setFailure(YoungerThanMinAgeFailure())
                else -> this.date.value = date
            }
        } else {
            this.date.value = null
        }
    }

    private fun setFailure(failure: Failure) {
        handleFailure(failure)
        this.date.value = null
    }

    fun onContinueClicked() {
        date.value?.let {
            continueClicked.postValue(BirthdateDataPoint(birthdate = it))
        }
    }

    private fun isUserLessThanMinAge(date: LocalDate) =
        ChronoUnit.YEARS.between(date, LocalDate.now()) < MIN_YEARS_OLD

    private fun isUserMoreThanMaxAge(date: LocalDate) =
        ChronoUnit.YEARS.between(date, LocalDate.now()) > MAX_YEARS_OLD

    class YoungerThanMinAgeFailure : Failure.FeatureFailure("birthday_collector_error_too_young")
    class OlderThanMaxAgeFailure : Failure.FeatureFailure("birthday_collector_error_too_old")
}
