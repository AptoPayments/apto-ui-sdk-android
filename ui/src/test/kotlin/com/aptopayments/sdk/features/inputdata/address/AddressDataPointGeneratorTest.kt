package com.aptopayments.sdk.features.inputdata.address

import com.google.android.libraries.places.api.model.AddressComponent
import com.google.android.libraries.places.api.model.AddressComponents
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private const val CITY = "locality"
private const val STREET = "route"
private const val NUMBER = "street_number"
private const val STATE = "administrative_area_level_1"
private const val COUNTRY = "country"
private const val POSTAL_CODE = "postal_code"

private const val VALUE_CITY_1 = "Paradise city"
private const val VALUE_STREET = "Green Grass"
private const val VALUE_NUMBER = "1"
private const val VALUE_STATE = "NY"
private const val VALUE_COUNTRY = "Paradise Country"
private const val VALUE_POSTAL_CODE = "1000"

class AddressDataPointGeneratorTest {

    private val sut = AddressDataPointGenerator()

    private val addressComponents = mock<AddressComponents>()

    @Test
    fun `when all values are set then address is correct`() {
        val list = createAddressComponentList()
        whenever(addressComponents.asList()).thenReturn(list)

        val result = sut.generate(addressComponents)

        assertNotNull(result)
        assertEquals(VALUE_CITY_1, result.locality)
        assertEquals("$VALUE_NUMBER $VALUE_STREET", result.streetOne)
        assertEquals(VALUE_STATE, result.region)
        assertEquals(VALUE_COUNTRY, result.country)
        assertEquals(VALUE_POSTAL_CODE, result.postalCode)
    }

    @Test
    fun `when city code is not present then address is null`() {
        generateListWithoutComponent(CITY)

        val result = sut.generate(addressComponents)

        assertNull(result)
    }

    @Test
    fun `when street is not present then address is null`() {
        generateListWithoutComponent(STREET)

        val result = sut.generate(addressComponents)

        assertNull(result)
    }

    @Test
    fun `when number is not present then address is null`() {
        generateListWithoutComponent(NUMBER)

        val result = sut.generate(addressComponents)

        assertNull(result)
    }

    @Test
    fun `when state is not present then address is null`() {
        generateListWithoutComponent(STATE)

        val result = sut.generate(addressComponents)

        assertNull(result)
    }

    @Test
    fun `when country code is not present then address is null`() {
        generateListWithoutComponent(COUNTRY)

        val result = sut.generate(addressComponents)

        assertNull(result)
    }

    @Test
    fun `when postal code is not present then address is null`() {
        generateListWithoutComponent(POSTAL_CODE)

        val result = sut.generate(addressComponents)

        assertNull(result)
    }

    private fun generateListWithoutComponent(componentToExclude: String) {
        val list = createAddressComponentList().filter { it.types.first() != componentToExclude }
        whenever(addressComponents.asList()).thenReturn(list)
    }

    private fun createAddressComponentList(): List<AddressComponent> {
        return listOf(
            createAddressComponent(CITY, VALUE_CITY_1),
            createAddressComponent(STREET, VALUE_STREET),
            createAddressComponent(NUMBER, VALUE_NUMBER),
            createAddressComponent(STATE, VALUE_STATE),
            createAddressComponent(COUNTRY, VALUE_COUNTRY),
            createAddressComponent(POSTAL_CODE, VALUE_POSTAL_CODE)
        )
    }

    private fun createAddressComponent(type: String, value: String) =
        mock<AddressComponent> {
            on { shortName } doReturn value
            on { name } doReturn value
            on { types } doReturn listOf(type)
        }
}
