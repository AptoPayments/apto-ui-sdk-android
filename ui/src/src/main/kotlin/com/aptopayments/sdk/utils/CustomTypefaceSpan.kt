package com.aptopayments.sdk.utils

import android.graphics.Paint
import android.text.style.MetricAffectingSpan
import android.graphics.Typeface
import android.text.TextPaint

/**
 * Custom span style to be able to customise the typeface on Android version older that P (API level 26).
 *
 * Taken from here: https://stackoverflow.com/a/17961854/1411844
 */
class CustomTypefaceSpan(private val typeface: Typeface) : MetricAffectingSpan() {
    override fun updateDrawState(drawState: TextPaint) {
        apply(drawState)
    }

    override fun updateMeasureState(paint: TextPaint) {
        apply(paint)
    }

    private fun apply(paint: Paint) {
        val oldTypeface = paint.typeface
        val oldStyle = oldTypeface?.style ?: 0
        val fakeStyle = oldStyle and typeface.style.inv()

        if (fakeStyle and Typeface.BOLD != 0) {
            paint.isFakeBoldText = true
        }

        if (fakeStyle and Typeface.ITALIC != 0) {
            paint.textSkewX = -0.25f
        }

        paint.typeface = typeface
    }
}
