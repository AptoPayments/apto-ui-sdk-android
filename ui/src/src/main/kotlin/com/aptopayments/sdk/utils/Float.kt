package com.aptopayments.sdk.utils

import android.content.res.Resources
import android.util.DisplayMetrics

internal fun Float.toDp(): Float {
    val metrics = Resources.getSystem().displayMetrics
    return this * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
}
