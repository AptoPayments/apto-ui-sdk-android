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

internal class AddressDataPointGenerator() {

    fun generate(components: AddressComponents, optional: String): AddressDataPoint? {
        val list = components.asList()
        val streetOne = getStreetOne(getStreetNumber(list), getStreet(list))
        val city = getCity(list)
        val state = getState(list)
        val country = getCountry(list)
        val postalCode = getPostalCode(list)
        return if (!isAnyRequiredFieldIncomplete(streetOne, city, state, country)) {
            AddressDataPoint(streetOne, optional, city, state, postalCode, country)
        } else {
            null
        }
    }

    private fun isAnyRequiredFieldIncomplete(streetOne: String, city: String, state: String, country: String) =
        streetOne.isEmpty() || city.isEmpty() || state.isEmpty() || country.isEmpty()

    private fun getStreetOne(number: String, street: String) = "$number${if (number.isEmpty()) "" else " "}$street"

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
}
