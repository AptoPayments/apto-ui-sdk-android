package com.aptopayments.sdk.utils.extensions

import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuff.Mode.*
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.RequiresApi

@SuppressWarnings("deprecation")
internal fun Drawable.setColorFilterCompat(@ColorInt color: Int, mode: PorterDuff.Mode) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        colorFilter = BlendModeColorFilter(color, getBlendMode(mode))
    } else {
        setColorFilter(color, mode)
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun getBlendMode(mode: PorterDuff.Mode): BlendMode {
    return when (mode) {
        CLEAR -> BlendMode.CLEAR
        SRC -> BlendMode.SRC
        DST -> BlendMode.DST
        SRC_OVER -> BlendMode.SRC_OVER
        DST_OVER -> BlendMode.DST_OVER
        SRC_IN -> BlendMode.SRC_IN
        DST_IN -> BlendMode.DST_IN
        SRC_OUT -> BlendMode.SRC_OUT
        DST_OUT -> BlendMode.DST_OUT
        SRC_ATOP -> BlendMode.SRC_ATOP
        DST_ATOP -> BlendMode.DST_ATOP
        XOR -> BlendMode.XOR
        DARKEN -> BlendMode.DARKEN
        LIGHTEN -> BlendMode.LIGHTEN
        MULTIPLY -> BlendMode.MULTIPLY
        SCREEN -> BlendMode.SCREEN
        ADD -> BlendMode.PLUS
        OVERLAY -> BlendMode.OVERLAY
    }
}
