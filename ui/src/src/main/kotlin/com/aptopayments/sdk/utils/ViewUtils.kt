package com.aptopayments.sdk.utils

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import android.util.DisplayMetrics
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AlertDialog
import com.hbb20.CountryCodePicker

object ViewUtils {

    fun hideKeyboard(context: Context) {
        try {
            val activity = context as Activity
            activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
            val windowToken = activity.currentFocus?.windowToken

            windowToken?.let {
                val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(it, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun showKeyboard(context: Context) {
        val activity = context as Activity
        val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    fun getAlertDialogBuilder(context: Context, okTitle: String, cancelTitle: String,
                              positiveCallback: () -> Unit,
                              negativeCallback: () -> Unit): AlertDialog.Builder {
        val builder = AlertDialog.Builder(context)
        builder.setPositiveButton(okTitle) { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
            positiveCallback()
        }
        builder.setNegativeButton(cancelTitle) { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
            negativeCallback()
        }
        return builder
    }

    fun isSmallScreen(context: Context): Boolean {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val metrics = DisplayMetrics()
        display.getMetrics(metrics)
        val screenHeight = metrics.heightPixels
        return screenHeight < 1800
    }
}

fun runOnUiThreadAfter(delay: Long, activity: Activity?, process: () -> Unit) {
    Handler().postDelayed({
        activity?.runOnUiThread { Runnable {
            process()
        } }
    }, delay)
}

fun disableCountryPicker(disable: Boolean, countryCodePicker: CountryCodePicker) {
    countryCodePicker.setCcpClickable(!disable)
    if (disable) countryCodePicker.setArrowSize(1)
}
