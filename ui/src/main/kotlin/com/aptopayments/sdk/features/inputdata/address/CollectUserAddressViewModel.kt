package com.aptopayments.sdk.features.inputdata.address

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.user.AddressDataPoint
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent
import com.aptopayments.sdk.utils.extensions.map
import com.google.android.libraries.places.api.model.AddressComponents
import kotlinx.coroutines.launch

internal class CollectUserAddressViewModel(
    private val initialValue: AddressDataPoint?,
    private val analyticsManager: AnalyticsServiceContract,
    private val addressGenerator: AddressDataPointGenerator,
    private val placesFetcher: PlaceFetcher
) : BaseViewModel() {

    private val addressDataPoint = MutableLiveData<AddressDataPoint?>()
    val searchText = MutableLiveData("")
    val optionalText = MutableLiveData("")
    val continueEnabled = addressDataPoint.map { it != null }
    val optionalVisible = continueEnabled.map { it }
    val continueClicked = LiveEvent<AddressDataPoint>()
    private val _showPoweredByGoogle = MutableLiveData(true)
    val showPoweredByGoogle = _showPoweredByGoogle as LiveData<Boolean>

    init {
        initialValue?.let {
            searchText.postValue("${it.streetOne}, ${it.locality}, ${it.country}")
            optionalText.postValue(it.streetTwo ?: "")
        }
        addressDataPoint.value = initialValue
    }

    fun viewLoaded() {
        analyticsManager.track(Event.WorkflowUserIdAddress)
    }

    fun continueClicked() {
        if (continueEnabled.value == true) {
            addressDataPoint.value = addStreetTwoToDataPoint(addressDataPoint.value!!, optionalText.value)
            continueClicked.postValue(addressDataPoint.value)
        }
    }

    private fun addStreetTwoToDataPoint(address: AddressDataPoint, streetTwo: String?): AddressDataPoint {
        return AddressDataPoint(
            streetOne = address.streetOne,
            streetTwo = streetTwo,
            locality = address.locality,
            region = address.region,
            postalCode = address.postalCode,
            country = address.country
        )
    }

    fun onAddressClicked(placeId: String) {
        viewModelScope.launch {
            val place = placesFetcher.fetchPlace(placeId)
            setAddressComponents(place?.addressComponents)
        }
        _showPoweredByGoogle.value = false
    }

    fun onEditingAddress() {
        _showPoweredByGoogle.value = true
        invalidateAddressComponents()
    }

    fun onAddressDismissed() {
        _showPoweredByGoogle.value = false
        invalidateAddressComponents()
    }

    private fun setAddressComponents(addressComponents: AddressComponents?) {
        addressDataPoint.value = addressComponents?.let { addressGenerator.generate(it) }
        checkIncorrectAddress()
    }

    private fun checkIncorrectAddress() {
        if (addressDataPoint.value == null) {
            handleFailure(IncorrectAddressFailure())
        }
    }

    private fun invalidateAddressComponents() {
        addressDataPoint.value = null
    }

    class IncorrectAddressFailure : Failure.FeatureFailure()
}
