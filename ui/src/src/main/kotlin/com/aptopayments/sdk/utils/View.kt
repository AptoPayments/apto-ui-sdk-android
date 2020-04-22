package com.aptopayments.sdk.utils

import android.view.View
import android.view.animation.AnimationUtils
import com.aptopayments.sdk.R

fun View.shake() {
    startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
}
