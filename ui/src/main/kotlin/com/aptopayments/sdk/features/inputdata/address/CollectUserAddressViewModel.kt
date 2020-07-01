package com.aptopayments.sdk.features.inputdata.address

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.user.AddressDataPoint
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent
import com.google.android.libraries.places.api.model.AddressComponents
import kotlinx.coroutines.launch

internal class CollectUserAddressViewModel(
    private val initialValue: AddressDataPoint?,
    private val analyticsManager: AnalyticsServiceContract,
    private val addressGenerator: AddressDataPointGenerator,
    private val placesFetcher: PlaceFetcher
) : BaseViewModel() {

    private var addressComponents: AddressComponents? = null
    val searchText = MutableLiveData("")
    val optionalText = MutableLiveData("")
    private val _continueEnabled = MutableLiveData(false)
    val continueEnabled = _continueEnabled as LiveData<Boolean>
    val optionalVisible = Transformations.map(_continueEnabled) { it }
    val continueClicked = LiveEvent<AddressDataPoint>()
    private val _showPoweredByGoogle = MutableLiveData(true)
    val showPoweredByGoogle = _showPoweredByGoogle as LiveData<Boolean>

    init {
        initialValue?.let {
            searchText.postValue("${it.streetOne}, ${it.locality}, ${it.country}")
            optionalText.postValue(it.streetTwo)
            _continueEnabled.postValue(true)
        }
    }

    fun viewLoaded() {
        analyticsManager.track(Event.WorkflowUserIdAddress)
    }

    fun continueClicked() {
        if (continueEnabled.value == true) {
            val dataPoint = if (addressComponents != null) {
                addressGenerator.generate(addressComponents!!, optionalText.value!!)
            } else {
                initialValue
            }
            continueClicked.postValue(dataPoint)
        }
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
        setAddressComponents(null)
    }

    fun onAddressDismissed() {
        _showPoweredByGoogle.value = false
        setAddressComponents(null)
    }

    private fun setAddressComponents(addressComponents: AddressComponents?) {
        this.addressComponents = addressComponents
        _continueEnabled.postValue(addressComponents != null)
    }
}
