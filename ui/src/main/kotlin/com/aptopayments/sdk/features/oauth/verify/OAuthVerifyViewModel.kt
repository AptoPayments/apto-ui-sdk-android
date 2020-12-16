package com.aptopayments.sdk.features.oauth.verify

import androidx.lifecycle.MutableLiveData
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.oauth.OAuthUserDataUpdate
import com.aptopayments.mobile.data.user.*
import com.aptopayments.mobile.data.workflowaction.AllowedBalanceType
import com.aptopayments.mobile.platform.AptoPlatform
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract

internal class OAuthVerifyViewModel(
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    val firstName: MutableLiveData<String> = MutableLiveData()
    val lastName: MutableLiveData<String> = MutableLiveData()
    val id: MutableLiveData<String> = MutableLiveData()
    val email: MutableLiveData<String> = MutableLiveData()
    val phone: MutableLiveData<String> = MutableLiveData()
    val address: MutableLiveData<String> = MutableLiveData()
    val birthdate: MutableLiveData<String> = MutableLiveData()

    fun setDataPoints(datapointList: DataPointList) {
        (datapointList.getUniqueDataPointOf(DataPoint.Type.NAME, null) as? NameDataPoint)?.let {
            firstName.value = it.firstName
            lastName.value = it.lastName
        }
        (datapointList.getUniqueDataPointOf(DataPoint.Type.ID_DOCUMENT, null) as? IdDocumentDataPoint)?.let {
            id.value = it.value
        }
        (datapointList.getUniqueDataPointOf(DataPoint.Type.EMAIL, null) as? EmailDataPoint)?.let {
            email.value = it.email
        }
        (datapointList.getUniqueDataPointOf(DataPoint.Type.PHONE, null) as? PhoneDataPoint)?.let {
            phone.value = it.phoneNumber.toStringRepresentation()
        }
        (datapointList.getUniqueDataPointOf(DataPoint.Type.ADDRESS, null) as? AddressDataPoint)?.let {
            address.value = it.toStringRepresentation()
        }
        (datapointList.getUniqueDataPointOf(DataPoint.Type.BIRTHDATE, null) as? BirthdateDataPoint)?.let {
            birthdate.value = it.toStringRepresentation()
        }
    }

    fun retrieveUpdatedUserData(
        allowedBalanceType: AllowedBalanceType,
        tokenId: String,
        callback: (oauthAttempt: OAuthUserDataUpdate) -> Unit
    ) {
        analyticsManager.track(Event.SelectBalanceStoreOauthConfirmRefreshDetailsTap)
        AptoPlatform.fetchOAuthData(allowedBalanceType, tokenId) { result ->
            result.either(::handleFailure) {
                it.userData?.let { dataPoints -> setDataPoints(dataPoints) }
                callback(it)
            }
        }
    }

    fun viewLoaded() {
        analyticsManager.track(Event.SelectBalanceStoreOauthConfirm)
    }
}
