package com.aptopayments.sdk.core.extension

import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.BaseTarget
import com.bumptech.glide.request.target.SizeReadyCallback
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition

fun View.cancelTransition() {
    transitionName = null
}

fun View.isVisible() = this.visibility == View.VISIBLE

fun View.show() = show(View.VISIBLE)

fun View.hide() = show(View.INVISIBLE)

fun View.remove() = show(View.GONE)

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

fun ViewGroup.inflate(@LayoutRes layoutRes: Int): View =
        LayoutInflater.from(context).inflate(layoutRes, this, false)

fun ImageView.loadFromUrl(url: String) =
        Glide.with(this.context.applicationContext)
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(this)!!

fun ImageView.loadFromUrlWithListener(url: String, listener: RequestListener<Drawable>) =
        Glide.with(this.context.applicationContext)
                .load(url)
                .transition(DrawableTransitionOptions.withCrossFade())
                .listener(listener)
                .into(this)!!

fun ImageView.loadUrlAndPostponeEnterTransition(url: String, activity: FragmentActivity) {
    val target: Target<Drawable> = ImageViewBaseTarget(this, activity)
    Glide.with(context.applicationContext).load(url).into(target)
}

fun View.setBackgroundColorKeepShape(@ColorInt color: Int) {
    this.background?.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP)
}

private class ImageViewBaseTarget (var imageView: ImageView?, var activity: FragmentActivity?) : BaseTarget<Drawable>() {
    override fun removeCallback(cb: SizeReadyCallback?) {
        imageView = null
        activity = null
    }

    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>) {
        imageView?.setImageDrawable(resource)
        activity?.supportStartPostponedEnterTransition()
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
        super.onLoadFailed(errorDrawable)
        activity?.supportStartPostponedEnterTransition()
    }

    override fun getSize(cb: SizeReadyCallback) = cb.onSizeReady(SIZE_ORIGINAL, SIZE_ORIGINAL)
}
