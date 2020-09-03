package com.aptopayments.sdk.core.extension

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import androidx.annotation.ColorInt
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener

fun View.isVisible() = this.visibility == View.VISIBLE

fun View.show() = show(View.VISIBLE)

fun View.hide() = show(View.INVISIBLE)

fun View.remove() = show(View.GONE)

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

private fun View.show(visibility: Int) { this.visibility = visibility }

fun ImageView.loadFromUrl(url: String) =
        Glide.with(this.context.applicationContext)
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(this)

fun ImageView.loadFromUrlWithListener(url: String, listener: RequestListener<Drawable>) =
        Glide.with(this.context.applicationContext)
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(listener)
                .into(this)

fun View.setBackgroundColorKeepShape(@ColorInt color: Int) {
    this.background?.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
}
