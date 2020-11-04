package com.aptopayments.sdk.utils.extensions

import android.view.View
import android.view.animation.AnimationUtils
import com.aptopayments.sdk.R
import com.aptopayments.sdk.utils.SafeClickListener

fun View.shake() {
    startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
}

internal fun View.setOnClickListenerSafe(onSafeClick: (View) -> Unit) {
    val safeClickListener = SafeClickListener {
        onSafeClick(it)
    }
    setOnClickListener(safeClickListener)
}
