package com.aptopayments.sdk.core.extension

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.View
import android.view.animation.AccelerateInterpolator
import androidx.annotation.ColorInt

fun View.isVisible() = this.visibility == View.VISIBLE

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.INVISIBLE
}

fun View.remove() {
    this.visibility = View.GONE
}

fun View.removeAnimated() {
    this.animate().alpha(0.0f).setInterpolator(AccelerateInterpolator()).setDuration(400)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                visibility = View.GONE
                alpha = 1.0f
            }
        })
}

fun View.invisibleIf(bool: Boolean) {
    this.visibility = if (bool) View.INVISIBLE else View.VISIBLE
}

fun View.goneIf(bool: Boolean) {
    this.visibility = if (bool) View.GONE else View.VISIBLE
}

fun View.visibleIf(bool: Boolean) {
    this.visibility = if (bool) View.VISIBLE else View.GONE
}

fun View.setBackgroundColorKeepShape(@ColorInt color: Int) {
    this.background?.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
}
