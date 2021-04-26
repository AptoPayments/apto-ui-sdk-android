package com.aptopayments.sdk.utils.extensions

import android.view.View
import android.view.animation.AnimationUtils
import androidx.annotation.RestrictTo
import com.aptopayments.sdk.R
import com.aptopayments.sdk.utils.SafeClickListener

fun View.shake() {
    startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun View.setOnClickListenerSafe(onSafeClick: (View) -> Unit) {
    setOnClickListener(SafeClickListener { onSafeClick(it) })
}
