package com.aptopayments.sdk.core.extension

import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener

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
