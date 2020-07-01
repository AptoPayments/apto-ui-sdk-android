package com.aptopayments.sdk.utils

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.aptopayments.mobile.data.config.UIConfig
import com.google.android.material.snackbar.Snackbar

class MessageBanner {

    enum class MessageType {
        ERROR,
        SUCCESS,
        HEADS_UP
    }

    fun showBanner(
        activity: FragmentActivity,
        textResId: Int,
        messageType: MessageType,
        duration: Int = Snackbar.LENGTH_LONG
    ) {
        if (activity.isFinishing) {
            return
        }
        showBanner(activity, activity.getString(textResId), messageType, duration)
    }

    fun showBanner(
        activity: FragmentActivity,
        message: String,
        messageType: MessageType,
        duration: Int = Snackbar.LENGTH_LONG,
        title: String? = null
    ) {
        if (activity.isFinishing) {
            return
        }

        val backgroundColor = when (messageType) {
            MessageType.ERROR -> UIConfig.uiErrorColor
            MessageType.SUCCESS -> UIConfig.uiSuccessColor
            MessageType.HEADS_UP -> UIConfig.uiPrimaryColor
        }

        val rootView = (activity.findViewById<View>(android.R.id.content) as ViewGroup)
            .getChildAt(0) as ViewGroup
        val customSnackbar = SnackbarThemeTwo.make(rootView, duration)
        val customTitle = if (UIConfig.showToastTitle) title else null
        customSnackbar.setTitle(customTitle)
        customSnackbar.setMessage(message)
        customSnackbar.setBackgroundColor(backgroundColor)
        customSnackbar.show()
    }
}
