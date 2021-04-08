package com.aptopayments.sdk.features.inputdata.address

import com.aptopayments.mobile.data.user.AddressDataPoint
import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.AddressComponents

private const val CITY_1 = "locality"
private const val CITY_2 = "postal_town"
private const val CITY_3 = "sublocality"
private const val STREET = "route"
private const val NUMBER = "street_number"
private const val STATE = "administrative_area_level_1"
private const val COUNTRY = "country"
private const val POSTAL_CODE = "postal_code"

internal class AddressDataPointGenerator {

    fun generate(components: AddressComponents): AddressDataPoint? {
        val address = Address(components.asList())
        return if (address.hasAllFieldsSet()) {
            AddressDataPoint(
                streetOne = "${address.streetNumber} ${address.street}",
                locality = address.city,
                region = address.state,
                postalCode = address.postalCode,
                country = address.country
            )
        } else {
            null
        }
    }
}

private class Address(components: List<AddressComponent>) {
    var streetNumber: String = ""
    var street: String = ""
    var city: String = ""
    var state: String = ""
    var country: String = ""
    var postalCode: String = ""

    init {
        streetNumber = getStreetNumber(components)
        street = getStreet(components)
        city = getCity(components)
        state = getState(components)
        country = getCountry(components)
        postalCode = getPostalCode(components)
    }

    private fun getStreetNumber(components: List<AddressComponent>) = getShortNameFor(components, NUMBER)

    private fun getStreet(components: List<AddressComponent>) =
        components.firstOrNull { it.types.first() == STREET }?.name ?: ""

    private fun getCity(components: List<AddressComponent>) =
        components.firstOrNull { it.types.intersect(setOf(CITY_1, CITY_2, CITY_3)).isNotEmpty() }?.name ?: ""

    private fun getState(components: List<AddressComponent>) = getShortNameFor(components, STATE)

    private fun getCountry(components: List<AddressComponent>) = getShortNameFor(components, COUNTRY)

    private fun getPostalCode(components: List<AddressComponent>) = getShortNameFor(components, POSTAL_CODE)

    private fun getShortNameFor(components: List<AddressComponent>, type: String) =
        components.firstOrNull { it.types.first() == type }?.shortName ?: ""

    fun hasAllFieldsSet() =
        streetNumber.isNotEmpty() &&
            street.isNotEmpty() &&
            city.isNotEmpty() &&
            state.isNotEmpty() &&
            country.isNotEmpty() &&
            postalCode.isNotEmpty()
}
