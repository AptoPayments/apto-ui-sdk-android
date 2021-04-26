package com.aptopayments.sdk.ui.views.birthdate

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Test
import kotlin.test.assertEquals

internal class FormatOrderGeneratorTest {

    private val provider: FormatOrderProvider = mock()
    val sut = FormatOrderGenerator(provider)

    @Test
    fun `given dmy as system format when getFormat order then DMY is parsed`() {
        whenever(provider.provide()).thenReturn(charArrayOf('d', 'm', 'y'))

        val order = sut.getFormatOrder()

        assertEquals(order, DateFormatOrder.DMY)
    }

    @Test
    fun `given mdy as system format when getFormat order then MDY is parsed`() {
        whenever(provider.provide()).thenReturn(charArrayOf('m', 'd', 'y'))

        val order = sut.getFormatOrder()

        assertEquals(order, DateFormatOrder.MDY)
    }
    @Test
    fun `given ymd as system format when getFormat order then YMD is parsed`() {
        whenever(provider.provide()).thenReturn(charArrayOf('y', 'm', 'd'))

        val order = sut.getFormatOrder()

        assertEquals(order, DateFormatOrder.YMD)
    }
    @Test
    fun `given other as system format when getFormat order then DMY is parsed`() {
        whenever(provider.provide()).thenReturn(charArrayOf('a', 's', 'd'))

        val order = sut.getFormatOrder()

        assertEquals(order, DateFormatOrder.DMY)
    }

    @Test
    fun `given exception is thrown then DMY is parsed`() {
        whenever(provider.provide()).thenThrow(IllegalArgumentException())

        val order = sut.getFormatOrder()

        assertEquals(order, DateFormatOrder.DMY)
    }
}
