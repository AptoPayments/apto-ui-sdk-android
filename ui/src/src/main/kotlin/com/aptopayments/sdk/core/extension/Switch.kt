package com.aptopayments.sdk.core.extension

import android.content.res.ColorStateList
import android.widget.Switch
import androidx.core.graphics.drawable.DrawableCompat
import com.aptopayments.core.extension.ColorParser
import com.aptopayments.core.extension.ColorParserImpl

fun Switch.setColor(foreground: Int, colorParser: ColorParser = ColorParserImpl()) {
    val foregroundColors = ColorStateList(
            arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
            intArrayOf(colorParser.fromHexString("FFF1F1F1", "FFF1F1F1"), foreground)
    )
    val backgroundColors = ColorStateList(
            arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
            intArrayOf(colorParser.fromHexString("40221F1F", "40221F1F"), foreground)
    )
    DrawableCompat.setTintList(DrawableCompat.wrap(thumbDrawable), foregroundColors)
    DrawableCompat.setTintList(DrawableCompat.wrap(trackDrawable), backgroundColors)
}

