package com.aptopayments.sdk.features.auth.birthdateverification

import androidx.lifecycle.MutableLiveData
import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.user.Verification
import com.aptopayments.core.extension.ISO8601
import com.aptopayments.core.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import java.util.*

internal class BirthdateVerificationViewModel constructor(
        private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel()
{
    var enableNextButton: MutableLiveData<Boolean> = MutableLiveData()
    var birthdateVerification: MutableLiveData<Verification> = MutableLiveData()
    private var mBirthdate: Date? = Date()
    private val AUTH_TYPE = "birthdate"

    fun finishVerification(verificationId: String, day: String, month: String, year: String) {
        setBirthdate(year, month, day)

        mBirthdate?.let { date ->
            val request = Verification(
                    verificationId = verificationId,
                    secret = ISO8601.formatDate(date),
                    verificationType = AUTH_TYPE)
            AptoPlatform.completeVerification(request) {
                it.either(::handleFailure, ::handleVerification)
            }
        }
    }

    private fun handleVerification(response: Verification) {
        birthdateVerification.postValue(response)
    }

    fun setBirthdate(year: String, monthOfYear: String, dayOfMonth: String) {
        try {
            val birth = GregorianCalendar(Integer.valueOf(year), Integer.valueOf(monthOfYear)-1, Integer.valueOf(dayOfMonth))
            birth.isLenient = false
            mBirthdate = birth.time
        } catch (iae: IllegalArgumentException) {
            mBirthdate = null
        }
    }

    fun viewLoaded() {
        analyticsManager.track(Event.AuthVerifyBirthdate)
    }
}
