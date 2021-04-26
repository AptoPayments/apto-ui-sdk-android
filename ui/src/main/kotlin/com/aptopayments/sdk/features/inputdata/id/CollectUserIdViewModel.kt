package com.aptopayments.sdk.features.inputdata.id

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.aptopayments.mobile.analytics.Event
import com.aptopayments.mobile.data.geo.Country
import com.aptopayments.mobile.data.user.IdDataPointConfiguration
import com.aptopayments.mobile.data.user.IdDocumentDataPoint
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract
import com.aptopayments.sdk.utils.LiveEvent
import java.lang.reflect.Modifier

internal const val UNSELECTED_VALUE = -1

internal class CollectUserIdViewModel(
    initialValue: IdDocumentDataPoint?,
    private val config: IdDataPointConfiguration,
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {
    private val ssnValidator by lazy { SSNValidator() }

    val countryList = config.allowedDocumentTypes.keys.toList()
    val selectedCountry = MutableLiveData<Country>()
    val countryIsVisible = config.allowedDocumentTypes.size > 1
    val typeList: LiveData<List<IdDocumentDataPoint.Type>> = Transformations.map(selectedCountry) {
        val typeList = config.allowedDocumentTypes[it] ?: listOf()
        if (typeList.size == 1) {
            typePosition.value = 0
        }
        typeList
    }

    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    val typePosition = MutableLiveData(UNSELECTED_VALUE)
    val number = MutableLiveData("")
    val continueEnabled = Transformations.map(number) { isAllRequiredDataSet() }
    val continueNext = LiveEvent<IdDocumentDataPoint>()

    init {
        analyticsManager.track(Event.WorkflowUserIdDocument)
        setInitialValuesIfPresent(initialValue)
        preselectCountryIfThereIsOnlyOne()
    }

    fun continueClicked() {
        if (continueEnabled.value == true) {
            continueNext.postValue(
                IdDocumentDataPoint(
                    typeList.value!![typePosition.value!!],
                    number.value!!,
                    selectedCountry.value!!.isoCode
                )
            )
        }
    }

    fun onIdTypeSelected(position: Int) {
        typePosition.value = position
    }

    private fun setInitialValuesIfPresent(initialValue: IdDocumentDataPoint?) {
        initialValue?.let {
            selectedCountry.value = Country(it.country!!)
            number.value = it.value
        }
    }

    private fun preselectCountryIfThereIsOnlyOne() {
        if (countryList.size == 1) {
            selectedCountry.value = countryList[0]
        }
    }

    private fun isAllRequiredDataSet() =
        selectedCountry.value != null &&
            typePosition.value != UNSELECTED_VALUE &&
            checkNumber(selectedCountry.value!!, typePosition.value, number.value)

    private fun checkNumber(country: Country, type: Int?, value: String?): Boolean {
        return when {
            isSSNinUS(country, type) -> !value.isNullOrEmpty() && ssnValidator.validate(value)
            else -> !value.isNullOrEmpty()
        }
    }

    private fun isSSNinUS(country: Country, type: Int?) =
        country.isoCode == "US" && type != null && typeList.value!![typePosition.value!!] == IdDocumentDataPoint.Type.SSN
}
