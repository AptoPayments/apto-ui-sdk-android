package com.aptopayments.sdk.features.auth.birthdateverification

import android.content.Context
import android.text.format.DateFormat
import java.util.*

internal class FormatOrderGenerator(private val context: Context) {

    fun getFormatOrder(): DateFormatOrder {
        val order = getDateOrderFromSystem()
        return parseFormatOrder(order)
    }

    private fun getDateOrderFromSystem() = DateFormat.getDateFormatOrder(context).joinToString("")

    private fun parseFormatOrder(order: String) =
        try {
            DateFormatOrder.valueOf(order.toUpperCase(Locale.ROOT))
        } catch (e: IllegalArgumentException) {
            DateFormatOrder.DMY
        }
}
