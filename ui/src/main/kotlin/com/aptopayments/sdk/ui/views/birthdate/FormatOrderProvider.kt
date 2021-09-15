package com.aptopayments.sdk.ui.views.birthdate

import android.content.Context
import android.text.format.DateFormat

internal class FormatOrderProvider(private val context: Context) {
    fun provide(): CharArray = DateFormat.getDateFormatOrder(context) ?: CharArray(0)
}
