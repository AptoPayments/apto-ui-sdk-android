package com.aptopayments.sdk.core.ui

import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.ColorInt
import com.aptopayments.core.extension.isDarkColor
import com.aptopayments.core.data.config.UIStatusBarStyle

object StatusBarUtil {

    fun setStatusBarColor(window: Window, @ColorInt color: Int, style: UIStatusBarStyle = UIStatusBarStyle.AUTO) {
        when (style) {
            UIStatusBarStyle.LIGHT -> setLightStatusBar(window, color)
            UIStatusBarStyle.DARK -> setDarkStatusBar(window, color)
            UIStatusBarStyle.AUTO ->
                if (isDarkColor(color)) setLightStatusBar(window, color)
                else setDarkStatusBar(window, color)
        }
    }

    private fun setDarkStatusBar(window: Window, @ColorInt color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val view: View = window.decorView.findViewById(android.R.id.content)
            var flags = view.systemUiVisibility
            flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            view.systemUiVisibility = flags
        }
        resetStatusBarColor(window, color)
    }

    private fun setLightStatusBar(window: Window, @ColorInt color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val view: View = window.decorView.findViewById(android.R.id.content)
            // Remove the light flag only if it was previously set
            var flags = view.systemUiVisibility
            val newFlags = flags xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            if ((flags and newFlags) == newFlags) {
                flags = flags xor View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            view.systemUiVisibility = flags
        }
        resetStatusBarColor(window, color)
    }

    private fun resetStatusBarColor(window: Window, @ColorInt color: Int) {
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = color
    }
}
