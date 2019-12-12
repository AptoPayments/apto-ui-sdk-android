package com.aptopayments.sdk.features.auth.birthdateverification

import com.aptopayments.sdk.AndroidTest
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

internal class FormatOrderGeneratorTest : AndroidTest() {

    val sut = FormatOrderGenerator(context())

    @Test
    fun whenUkThenDMY() {
        Locale.setDefault(Locale.UK)

        val order = sut.getFormatOrder()

        assertEquals(order, DateFormatOrder.DMY)
    }

    @Test
    fun whenSpainThenDMY() {
        Locale.setDefault(Locale.forLanguageTag("es-ES"))

        val order = sut.getFormatOrder()

        assertEquals(order, DateFormatOrder.DMY)
    }

    @Test
    fun whenUsaThenMDY() {
        Locale.setDefault(Locale.US)

        val order = sut.getFormatOrder()

        assertEquals(order, DateFormatOrder.MDY)
    }

    @Test
    fun whenJapanThenYMD() {
        Locale.setDefault(Locale.JAPAN)

        val order = sut.getFormatOrder()

        assertEquals(order, DateFormatOrder.YMD)
    }

    @Test
    fun whenChinaThenYMD() {
        Locale.setDefault(Locale.CHINA)

        val order = sut.getFormatOrder()

        assertEquals(order, DateFormatOrder.YMD)
    }

    companion object {
        private lateinit var configuredLocale: Locale

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            configuredLocale = Locale.getDefault()
        }

        @AfterClass
        @JvmStatic
        fun afterClass() {
            Locale.setDefault(configuredLocale)
        }
    }
}
