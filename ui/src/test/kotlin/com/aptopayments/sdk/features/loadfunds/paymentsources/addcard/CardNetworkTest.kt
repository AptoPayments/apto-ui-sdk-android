package com.aptopayments.sdk.features.loadfunds.paymentsources.addcard

import com.aptopayments.sdk.R
import com.aptopayments.sdk.features.loadfunds.paymentsources.addcard.CardNetwork.UNKNOWN
import org.junit.Test
import kotlin.test.assertEquals

internal class CardNetworkTest {

    @Test
    fun `every element has an icon`() {
        CardNetwork.values().forEach {
            assert(it.icon > 0)
        }
    }

    @Test
    fun `unknown has the higher maxLenghValue `() {
        val maxLength = CardNetwork.values().maxBy { it.maxLength }?.maxLength ?: 0

        assertEquals(UNKNOWN.maxLength, maxLength)
    }

    @Test
    fun `unknown has the default values correct`() {
        assertEquals("", UNKNOWN.networkPattern)
        assertEquals("", UNKNOWN.pattern)
        assertEquals(0, UNKNOWN.cvvDigits)
        assertEquals(R.drawable.ic_card_unknown, UNKNOWN.icon)
    }
}
