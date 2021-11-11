package com.aptopayments.sdk.ui.views.birthdate

import java.util.Locale

internal class FormatOrderGenerator(private val provider: FormatOrderProvider) {

    fun getFormatOrder(): DateFormatOrder {
        return try {
            DateFormatOrder.valueOf(getDateOrderFromSystem())
        } catch (e: IllegalArgumentException) {
            DateFormatOrder.DMY
        }
    }

    private fun getDateOrderFromSystem() = provider.provide().joinToString("").uppercase(Locale.ROOT)
}
