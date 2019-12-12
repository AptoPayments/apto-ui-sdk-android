package com.aptopayments.sdk.features.auth.birthdateverification

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.user.Verification
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent
import org.koin.core.KoinComponent
import org.koin.core.inject
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

private const val AUTH_TYPE = "birthdate"

internal class BirthdateVerificationViewModel constructor(
    private val analyticsManager: AnalyticsServiceContract,
    formatOrderGenerator: FormatOrderGenerator
) : BaseViewModel(), KoinComponent {

    private var day = ""
    private var month = ""
    private var year = ""
    private val _continueEnabled = MutableLiveData(false)
    val dateOrder: LiveData<DateFormatOrder>
    val continueEnabled = _continueEnabled as LiveData<Boolean>
    val birthdateVerification = LiveEvent<Verification>()

    init {
        dateOrder = MutableLiveData(formatOrderGenerator.getFormatOrder())
    }

    private fun parseDate(year: String, monthOfYear: String, dayOfMonth: String): LocalDate? {
        return try {
            LocalDate.of(year.toInt(), monthOfYear.toInt(), dayOfMonth.toInt())
        } catch (iae: IllegalArgumentException) {
            null
        }
    }

    fun viewLoaded() {
        analyticsManager.track(Event.AuthVerifyBirthdate)
    }

    fun setDay(text: String) {
        day = text
        checkDate()
    }

    private fun checkDate() {
        _continueEnabled.value = areAllValuesPresent() && parseDate(year, month, day) != null
    }

    private fun areAllValuesPresent() = day.isNotEmpty() && month.isNotEmpty() && year.isNotEmpty()

    fun setMonth(text: String) {
        month = text
        checkDate()
    }

    fun setYear(text: String) {
        year = text
        checkDate()
    }

    fun onContinueButtonPressed(verificationId: String) {
        val date = parseDate(year, month, day)
        val request = getRequestParams(verificationId, date!!)
        AptoPlatform.completeVerification(request) {
            it.either(::handleFailure, ::handleVerification)
        }
    }

    private fun getRequestParams(verificationId: String, date: LocalDate) =
        Verification(
            verificationId = verificationId,
            secret = date.format(DateTimeFormatter.ISO_DATE),
            verificationType = AUTH_TYPE
        )

    private fun handleVerification(response: Verification) {
        birthdateVerification.postValue(response)
    }

}
