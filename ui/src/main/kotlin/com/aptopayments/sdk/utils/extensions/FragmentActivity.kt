package com.aptopayments.sdk.utils.extensions

import android.view.View
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.fragment.app.FragmentActivity
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.utils.CustomSnackbar
import com.google.android.material.snackbar.Snackbar

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun FragmentActivity.showCustomSnackbar(
    message: String,
    messageType: SnackbarMessageType,
    duration: Int = Snackbar.LENGTH_LONG,
    title: String? = null
) {
    if (!isFinishing && !(message.isEmpty() && title.isNullOrEmpty())) {
        val backgroundColor = when (messageType) {
            SnackbarMessageType.ERROR -> UIConfig.uiErrorColor
            SnackbarMessageType.SUCCESS -> UIConfig.uiSuccessColor
            SnackbarMessageType.HEADS_UP -> UIConfig.uiPrimaryColor
        }

        val rootView = (findViewById<View>(android.R.id.content) as ViewGroup)
            .getChildAt(0) as ViewGroup
        val customSnackbar = CustomSnackbar.make(rootView, duration)
        val customTitle = if (UIConfig.showToastTitle) title else null
        customSnackbar.setTitle(customTitle)
        customSnackbar.setMessage(message)
        customSnackbar.setBackgroundColor(backgroundColor)
        customSnackbar.show()
    }
}

enum class SnackbarMessageType {
    ERROR,
    SUCCESS,
    HEADS_UP
}
