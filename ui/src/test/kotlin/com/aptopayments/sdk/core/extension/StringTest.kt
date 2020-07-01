package com.aptopayments.sdk.core.extension

import com.aptopayments.sdk.UnitTest
import org.junit.Assert.assertEquals
import org.junit.Test

class StringTest : UnitTest() {

    @Test
    fun `stars_ when empty string then empty`() {
        assertEquals("".starsExceptLast(), "")
    }

    @Test
    fun `stars_ when one digit then that digit`() {
        assertEquals("1".starsExceptLast(), "1")
    }

    @Test
    fun `stars_ when two digits then one star and one digit`() {
        assertEquals("12".starsExceptLast(), "*2")
    }

    @Test
    fun `stars_ when three digits then two star and one digit`() {
        assertEquals("123".starsExceptLast(), "**3")
    }

    @Test
    fun `stars_ when four digits then three star and one digit`() {
        assertEquals("1234".starsExceptLast(), "***4")
    }
}
