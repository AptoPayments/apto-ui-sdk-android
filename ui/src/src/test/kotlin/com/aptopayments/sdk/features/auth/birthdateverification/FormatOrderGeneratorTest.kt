package com.aptopayments.sdk.features.auth.birthdateverification

import com.aptopayments.sdk.AndroidTest
import com.aptopayments.sdk.ui.views.birthdate.DateFormatOrder
import com.aptopayments.sdk.ui.views.birthdate.FormatOrderGenerator
import org.junit.Test
import org.robolectric.annotation.Config
import java.util.Locale
import kotlin.test.assertEquals

internal class FormatOrderGeneratorTest : AndroidTest() {

    val sut = FormatOrderGenerator(context())

    @Test @Config(qualifiers = "en-rGB")
    fun whenUkThenDMY() {
        val order = sut.getFormatOrder()

        assertEquals(order, DateFormatOrder.DMY)
    }

    @Test @Config(qualifiers = "es-rES")
    fun whenSpainThenDMY() {
        val order = sut.getFormatOrder()

        assertEquals(order, DateFormatOrder.DMY)
    }

    @Test @Config(qualifiers = "en-rUS")
    fun whenUsaThenMDY() {

        val order = sut.getFormatOrder()

        assertEquals(order, DateFormatOrder.MDY)
    }

    @Test @Config(qualifiers = "ja-rJP")
    fun whenJapanThenYMD() {
        val order = sut.getFormatOrder()

        assertEquals(order, DateFormatOrder.YMD)
    }

    @Test @Config(qualifiers = "zh-rCN")
    fun whenChinaThenYMD() {
        Locale.setDefault(Locale.CHINA)

        val order = sut.getFormatOrder()

        assertEquals(order, DateFormatOrder.YMD)
    }
}
