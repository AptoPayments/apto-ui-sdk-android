package com.aptopayments.sdk.ui.views.birthdate

import android.content.Context
import android.text.format.DateFormat
import java.util.Locale

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
