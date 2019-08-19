package com.aptopayments.sdk.core.platform.theme

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.util.TypedValue
import android.view.Window
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatRadioButton
import com.aptopayments.sdk.R
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.extension.adjustColorValue
import com.aptopayments.sdk.core.extension.setBackgroundColorKeepShape
import com.aptopayments.sdk.core.ui.StatusBarUtil
import com.google.android.material.appbar.AppBarLayout

internal object ThemeOneManager: ThemeManager {
    override fun customizeToolbarTitle(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeHighlightTitleLabel(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeFooterLabel(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeErrorLabel(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeStatusBar(window: Window) {
        setStatusBarColor(window, UIConfig.uiNavigationPrimaryColor)
    }

    override fun customizeSecondaryNavigationStatusBar(window: Window) {
        setStatusBarColor(window, UIConfig.uiNavigationSecondaryColor)
    }

    override fun customizeSecondaryNavigationToolBar(appBarLayout: AppBarLayout) {
        appBarLayout.setBackgroundColorKeepShape(UIConfig.uiNavigationPrimaryColor)
    }

    private fun setStatusBarColor(window: Window, @ColorInt color: Int) {
        val statusBarColor = adjustColorValue(color, 0.8f)
        StatusBarUtil.setStatusBarColor(window, statusBarColor, UIConfig.uiStatusBarStyle)
    }

    override fun customizeSubmitButton(textView: TextView) {
        textView.apply {
            typeface = Typeface.DEFAULT
            setBackgroundColorKeepShape(UIConfig.uiPrimaryColor)
            setTextColor(UIConfig.textButtonColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        }
    }

    override fun customizeLargeTitleLabel(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeRegularTextLabel(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeSectionHeader(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeFormLabel(textView: TextView) {
        textView.apply {
            setTypeface(textView.typeface, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTextColor(UIConfig.textPrimaryColor)
        }
    }

    override fun customizeFormTextLink(textView: TextView) {
        textView.apply {
            setTypeface(textView.typeface, Typeface.BOLD)
            setTextColor(UIConfig.textSecondaryColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setLinkTextColor(UIConfig.textSecondaryColor)
            if (UIConfig.underlineLinks) paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
    }

    override fun customizeHtml(textView: TextView, html: Spanned) {
        textView.apply {
            typeface = Typeface.DEFAULT
            setTextColor(UIConfig.textPrimaryColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        }
        val spannable = SpannableStringBuilder(html)
        spannable.getSpans(0, spannable.length, URLSpan::class.java).forEach {
            val start = spannable.getSpanStart(it)
            val end = spannable.getSpanEnd(it)
            applyStyle(ForegroundColorSpan(UIConfig.textLinkColor), spannable, start, end)
            applyStyle(StyleSpan(Typeface.BOLD), spannable, start, end)
            if (UIConfig.underlineLinks) applyStyle(UnderlineSpan(), spannable, start, end)
        }
    }

    override fun customizeContentPlainText(textView: TextView) {
        textView.apply {
            typeface = Typeface.DEFAULT
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(UIConfig.textTertiaryColor)
        }
    }

    override fun customizeContentPlainInvertedText(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeSectionTitle(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeSectionOptionTitle(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeSectionOptionDescription(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeSectionOptionIcon(imageView: ImageView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeSwitch(switch: Switch) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeMainItem(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeMainItemRight(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeTimestamp(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeStarredSectionTitle(textView: TextView, @ColorInt textColor: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeAmountBig(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeAmountMedium(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeAmountSmall(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeSubCurrency(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeCardSmallValue(context: Context, textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeCardLargeValue(context: Context, textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeEmptyCase(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeBannerTitle(textView: TextView) {
        textView.apply {
            setTypeface(textView.typeface, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(UIConfig.textMessageColor)
        }
    }

    override fun customizeBannerMessage(textView: TextView) {
        textView.apply {
            setTypeface(textView.typeface, Typeface.BOLD)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTextColor(UIConfig.textMessageColor)
            maxLines = 100 // Trick to be able to show multiple lines
        }
    }

    override fun getAlertDialog(builder: AlertDialog.Builder, alertTitle: String,
                                alertMessage: String): AlertDialog {
        val view = builder.create().layoutInflater.inflate(R.layout.alertview_theme_one, null)
        val dialogTitle = view.findViewById(R.id.alert_title) as TextView
        dialogTitle.apply {
            text = alertTitle
            setTextColor(UIConfig.textPrimaryColor)
        }

        val dialogMessage = view.findViewById(R.id.alert_message) as TextView
        dialogMessage.apply {
            text = alertMessage
            setTextColor(UIConfig.textSecondaryColor)
        }

        val dialog = builder.create()
        dialog.setView(view)

        dialog.show() // Required to format buttons

        val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        cancelButton.setTextColor(UIConfig.uiPrimaryColor)

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(UIConfig.uiPrimaryColor)

        Typeface.DEFAULT_BOLD.let {
            dialogTitle.typeface = it
            positiveButton.typeface = it
            cancelButton.typeface = it
        }
        dialogMessage.typeface = Typeface.DEFAULT
        return dialog
    }

    override fun customizeRadioButton(button: AppCompatRadioButton) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeMenuItem(textView: TextView) {
        textView.apply {
            setTypeface(textView.typeface, Typeface.BOLD)
            setBackgroundColorKeepShape(UIConfig.uiPrimaryColor)
            setTextColor(UIConfig.textTopBarPrimaryColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
        }
    }

    override fun customizeFormField(textView: TextView) {
        textView.apply {
            setTypeface(textView.typeface, Typeface.NORMAL)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            setTextColor(UIConfig.textSecondaryColor)
        }
    }

    override fun customizeFormFieldSmall(textView: TextView) {
        textView.apply {
            setTypeface(textView.typeface, Typeface.NORMAL)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            setTextColor(UIConfig.textSecondaryColor)
        }
    }

    override fun customizeCheckBox(checkBox: AppCompatCheckBox) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

