package com.aptopayments.sdk.core.extension

import android.widget.TextView

fun TextView.interpolateTextSize(startSize: Float, endSize: Float, fraction: Float) {
    this.textSize = endSize - ((endSize - startSize) * fraction)
}
