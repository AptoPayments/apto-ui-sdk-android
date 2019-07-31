package com.aptopayments.sdk.utils

import com.google.i18n.phonenumbers.NumberParseException
import com.aptopayments.sdk.UnitTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class StringUtilsTest : UnitTest() {

    private val sut = StringUtils

    @Test
    fun `given an empty string will throw NumberParseException`() {
        // Given
        val number = ""

        // Then
        assertFailsWith<NumberParseException> { sut.parsePhoneNumber(number) }
    }

    @Test
    fun `given a string without a leading + will throw NumberParseException`() {
        // Given
        val number = "34666555444"

        // Then
        assertFailsWith<NumberParseException> { sut.parsePhoneNumber(number) }
    }

    @Test
    fun `given a number with country code and national number will return a PhoneNumber`() {
        // Given
        val testCountryCode = "34"
        val testNumber = "666555444"

        // When
        val result = sut.parsePhoneNumber("+$testCountryCode$testNumber")

        // Then
        assertNotNull(result)
        assertEquals(result.countryCode, testCountryCode)
        assertEquals(result.phoneNumber, testNumber)
    }
}
