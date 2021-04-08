package com.aptopayments.sdk.features.inputdata.id

import org.junit.Assert.*
import org.junit.Test

private const val TEST_SSN_NUMBER = "778628144"
private const val TEST_SSN_NUMBER_WITH_SPACES = "778 62 8144"
private const val TEST_SSN_NUMBER_WITH_HYPHENS = "778-62-8144"
private const val TEST_SSN_NUMBER_WITH_BOTH = "778-62 8144"

class SSNValidatorTest {

    private val sut = SSNValidator()

    @Test
    fun `given null when validate then false`() {
        val result = sut.validate(null)

        assertFalse(result)
    }

    @Test
    fun `given empty when validate then false`() {
        val result = sut.validate("")

        assertFalse(result)
    }

    @Test
    fun `given wrong number when validate then false`() {
        val result = sut.validate("1")

        assertFalse(result)
    }

    @Test
    fun `given wrong number with correct digit amount when validate then false`() {
        val result = sut.validate("000000000")

        assertFalse(result)
    }

    @Test
    fun `given correct number missing one digit when validate then false`() {
        val result = sut.validate(TEST_SSN_NUMBER.removeRange(7..8))

        assertFalse(result)
    }

    @Test
    fun `given correct number when validate then true`() {
        val result = sut.validate(TEST_SSN_NUMBER)

        assertTrue(result)
    }

    @Test
    fun `given correct number with spaces when validate then true`() {
        val result = sut.validate(TEST_SSN_NUMBER_WITH_SPACES)

        assertTrue(result)
    }

    @Test
    fun `given correct number with hyphens when validate then true`() {
        val result = sut.validate(TEST_SSN_NUMBER_WITH_HYPHENS)

        assertTrue(result)
    }

    @Test
    fun `given correct number with hyphens and spaces when validate then true`() {
        val result = sut.validate(TEST_SSN_NUMBER_WITH_BOTH)

        assertTrue(result)
    }

    @Test
    fun `given correct number with a letter at the beginning when validate then false`() {
        val result = sut.validate(TEST_SSN_NUMBER.plus("a"))

        assertFalse(result)
    }

    @Test
    fun `given correct number with a letter at the middle when validate then false`() {
        val asd = TEST_SSN_NUMBER.chunked(5)
        val result = sut.validate("${asd[0]}a${asd[1]}")

        assertFalse(result)
    }
}
