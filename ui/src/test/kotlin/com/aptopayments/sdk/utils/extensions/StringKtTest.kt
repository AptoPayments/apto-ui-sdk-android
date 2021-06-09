package com.aptopayments.sdk.utils.extensions

import com.google.i18n.phonenumbers.NumberParseException
import org.junit.jupiter.api.Test
import kotlin.test.*
import kotlin.test.assertFalse

class StringKtTest {

    @Test
    fun `toCapitalized whenever empty string then empty`() {
        val result = "".toCapitalized()

        assertEquals("", result)
    }

    @Test
    fun `toCapitalized whenever one character string then it gets capitalized`() {
        val result = "a".toCapitalized()

        assertEquals("A", result)
    }

    @Test
    fun `toCapitalized whenever one word then only first letter gets capitalized`() {
        val result = "asd".toCapitalized()

        assertEquals("Asd", result)
    }

    @Test
    fun `toCapitalized whenever many words then only first letters get capitalized`() {
        val result = "asd fgh".toCapitalized()

        assertEquals("Asd Fgh", result)
    }

    @Test
    fun `toCapitalized whenever many words in capital then only first letters get capitalized`() {
        val result = "ASD FGH".toCapitalized()

        assertEquals("Asd Fgh", result)
    }

    @Test
    fun `isValidEmail whenever empty string email then false`() {
        assertFalse("".isValidEmail())
    }

    @Test
    fun `isValidEmail whenever one character string email then false`() {
        assertFalse("a".isValidEmail())
    }

    @Test
    fun `isValidEmail whenever valid email then true`() {
        assertTrue("a@a.com".isValidEmail())
    }

    @Test
    fun `parsePhoneNumber given an empty string will throw NumberParseException`() {
        // Given
        val number = ""

        // Then
        assertFailsWith<NumberParseException> { number.parsePhoneNumber() }
    }

    @Test
    fun `parsePhoneNumber given a string without a leading + will throw NumberParseException`() {
        // Given
        val number = "34666555444"

        // Then
        assertFailsWith<NumberParseException> { number.parsePhoneNumber() }
    }

    @Test
    fun `parsePhoneNumber given a number with country code and national number will return a PhoneNumber`() {
        // Given
        val testCountryCode = "34"
        val testNumber = "666555444"

        // When
        val result = "+$testCountryCode$testNumber".parsePhoneNumber()

        // Then
        assertNotNull(result)
        assertEquals(result.countryCode, testCountryCode)
        assertEquals(result.phoneNumber, testNumber)
    }
}
