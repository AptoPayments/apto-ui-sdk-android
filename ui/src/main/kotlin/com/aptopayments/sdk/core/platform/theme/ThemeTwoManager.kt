package com.aptopayments.sdk.core.platform.theme

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.*
import android.util.TypedValue
import android.view.Gravity
import android.view.Window
import android.widget.*
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.graphics.ColorUtils
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.setBackgroundColorKeepShape
import com.aptopayments.sdk.core.extension.setColor
import com.aptopayments.sdk.core.ui.StatusBarUtil
import com.aptopayments.sdk.utils.CustomTypefaceSpan
import com.aptopayments.sdk.utils.FontsUtil
import com.aptopayments.sdk.utils.FontsUtil.FontType.*
import com.aptopayments.sdk.utils.extensions.toDp
import com.google.android.material.appbar.AppBarLayout

private const val CARD_FONT_FILE = "fonts/ocraextended.ttf"
private const val OPACITY_50_PERCENT = 128

internal object ThemeTwoManager : ThemeManager {
    private var mCardTypeface: Typeface? = null
    private fun getCardTypeface(context: Context): Typeface {
        return mCardTypeface ?: {
            mCardTypeface = Typeface.createFromAsset(context.assets, CARD_FONT_FILE)
            mCardTypeface!!
        }()
    }

    override fun customizeStatusBar(window: Window) {
        StatusBarUtil.setStatusBarColor(window, UIConfig.uiNavigationPrimaryColor, UIConfig.uiStatusBarStyle)
    }

    override fun customizeSecondaryNavigationStatusBar(window: Window) {
        StatusBarUtil.setStatusBarColor(window, UIConfig.uiNavigationSecondaryColor, UIConfig.uiStatusBarStyle)
    }

    override fun customizeSecondaryNavigationToolBar(appBarLayout: AppBarLayout) {
        appBarLayout.setBackgroundColorKeepShape(UIConfig.uiBackgroundPrimaryColor)
    }

    override fun customizeToolbarTitle(textView: TextView) {
        setFontType(textView, BOLD)
        textView.apply {
            setTextColor(UIConfig.textTopBarSecondaryColor)
            setLineSpacing(0f, 1.17f)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
        }
    }

    override fun customizeHighlightTitleLabel(textView: TextView) {
        setFontType(textView, BOLD)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
        textView.setTextColor(UIConfig.textSecondaryColor)
    }

    override fun customizeSubmitButton(textView: TextView) {
        setButtonFont(textView)
        val color = UIConfig.uiPrimaryColor
        val cornerRadius = UIConfig.buttonCornerRadius
        val enabledBackground = buttonGradientDrawable(cornerRadius, color)
        val disabledBackground = buttonGradientDrawable(cornerRadius, color, (255 * 0.3).toInt())
        val states = StateListDrawable()
        states.addState(intArrayOf(android.R.attr.state_enabled), enabledBackground)
        states.addState(intArrayOf(-android.R.attr.state_enabled), disabledBackground)
        textView.background = states
    }

    override fun customizeColorlessButton(textView: TextView) {
        setButtonFont(textView)
        textView.setTextColor(UIConfig.textPrimaryColor)
    }

    private fun setButtonFont(textView: TextView) {
        setFontType(textView, MEDIUM)
        textView.apply {
            setTextColor(UIConfig.textButtonColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        }
    }

    private fun buttonGradientDrawable(cornerRadius: Float, color: Int, alpha: Int = 255): GradientDrawable {
        val disabledBackground = GradientDrawable()
        disabledBackground.cornerRadius = cornerRadius.toDp()
        disabledBackground.setColor(color)
        disabledBackground.alpha = alpha
        return disabledBackground
    }

    override fun customizeLargeTitleLabel(textView: TextView) {
        setFontType(textView, BOLD)
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
            setTextColor(UIConfig.textPrimaryColor)
        }
    }

    override fun customizeRegularTextLabel(textView: TextView) {
        setFontType(textView, REGULAR)
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(UIConfig.textSecondaryColor)
        }
    }

    override fun customizeSectionHeader(textView: TextView) {
        setFontType(textView, REGULAR)
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(UIConfig.textSecondaryColor)
        }
    }

    override fun customizeFormLabel(textView: TextView) {
        setFontType(textView, REGULAR)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        textView.setTextColor(UIConfig.textSecondaryColor)
    }

    override fun customizeFooterLabel(textView: TextView) {
        setFontType(textView, REGULAR)
        textView.apply {
            setTextColor(UIConfig.textSecondaryColor)
            setLinkTextColor(UIConfig.textLinkColor)
        }
    }

    override fun customizeErrorLabel(textView: TextView) {
        setFontType(textView, SEMI_BOLD)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        textView.apply {
            setTextColor(UIConfig.uiErrorColor)
        }
    }

    override fun customizeFormTextLink(textView: TextView) {
        setFontType(textView, SEMI_BOLD)
        textView.apply {
            setTextColor(UIConfig.textLinkColor)
            setLinkTextColor(UIConfig.textLinkColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            if (UIConfig.underlineLinks) paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
    }

    override fun customizeHtml(textView: TextView, html: Spanned) {
        setFontType(textView, REGULAR)
        textView.apply {
            setTextColor(UIConfig.textPrimaryColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        }
        val spannable = SpannableStringBuilder(html)
        spannable.getSpans(0, spannable.length, URLSpan::class.java).forEach {
            val start = spannable.getSpanStart(it)
            val end = spannable.getSpanEnd(it)
            applyStyle(ForegroundColorSpan(UIConfig.textLinkColor), spannable, start, end)
            FontsUtil.getFontOfType(SEMI_BOLD)?.let { typeface ->
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    applyStyle(TypefaceSpan(typeface), spannable, start, end)
                } else {
                    applyStyle(CustomTypefaceSpan(typeface), spannable, start, end)
                }
            } ?: applyStyle(StyleSpan(Typeface.BOLD), spannable, start, end)
            if (UIConfig.underlineLinks) applyStyle(UnderlineSpan(), spannable, start, end)
        }
        textView.text = spannable
    }

    override fun customizeContentPlainText(textView: TextView) {
        setFontType(textView, REGULAR)
        textView.setTextColor(UIConfig.textPrimaryColor)
    }

    override fun customizeContentPlainInvertedText(textView: TextView) {
        setFontType(textView, REGULAR)
        textView.setTextColor(UIConfig.textTopBarSecondaryColor)
    }

    override fun customizeMainItem(textView: TextView) {
        setFontType(textView, MEDIUM)
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTextColor(UIConfig.textPrimaryColor)
            setLineSpacing(0f, 1.60f)
        }
    }

    override fun customizeMainItemRight(textView: TextView) {
        setFontType(textView, REGULAR)
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTextColor(UIConfig.textTertiaryColor)
            setLineSpacing(0f, 1.60f)
            gravity = Gravity.END
        }
    }

    override fun customizeTimestamp(textView: TextView) {
        setFontType(textView, REGULAR)
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(UIConfig.textTertiaryColor)
            setLineSpacing(0f, 1.17f)
        }
    }

    override fun customizeSectionTitle(textView: TextView) {
        setFontType(textView, MEDIUM)
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(UIConfig.textSecondaryColor)
        }
    }

    override fun customizeSectionOptionTitle(textView: TextView) {
        setFontType(textView, MEDIUM)
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(UIConfig.textSecondaryColor)
        }
    }

    override fun customizeSectionOptionDescription(textView: TextView) {
        setFontType(textView, REGULAR)
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(UIConfig.textTertiaryColor)
        }
    }

    override fun customizeSectionOptionIcon(imageView: ImageView) {
        imageView.setColorFilter(UIConfig.uiTertiaryColor)
    }

    override fun customizeSwitch(switch: Switch) {
        switch.setColor(foreground = UIConfig.uiPrimaryColor)
    }

    override fun customizeStarredSectionTitle(textView: TextView, @ColorInt textColor: Int) {
        setFontType(textView, REGULAR)
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            alpha = 0.7f
            setTextColor(textColor)
            setAllCaps(true)
            letterSpacing = 0.25f
            setLineSpacing(0f, 1.17f)
        }
    }

    override fun customizeAmountBig(textView: TextView) {
        setFontType(textView, BOLD)
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 31f)
            setTextColor(UIConfig.textTopBarSecondaryColor)
        }
    }

    override fun customizeAmountMedium(textView: TextView) {
        setFontType(textView, MEDIUM)
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(UIConfig.textSecondaryColor)
        }
    }

    override fun customizeAmountSmall(textView: TextView) {
        setFontType(textView, MEDIUM)
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTextColor(UIConfig.textPrimaryColor)
        }
    }

    override fun customizeSubCurrency(textView: TextView) {
        setFontType(textView, REGULAR)
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            alpha = 0.7f
            setTextColor(UIConfig.textTopBarSecondaryColor)
        }
    }

    override fun customizeEmptyCase(textView: TextView) {
        setFontType(textView, MEDIUM)
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(UIConfig.textTertiaryColor)
            gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
        }
    }

    override fun customizeBannerTitle(textView: TextView) {
        setFontType(textView, MEDIUM)
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(UIConfig.textMessageColor)
        }
    }

    override fun customizeBannerMessage(textView: TextView) {
        setFontType(textView, REGULAR)
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTextColor(UIConfig.textMessageColor)
            maxLines = 100 // Trick to be able to show multiple lines
        }
    }

    override fun getAlertDialog(builder: AlertDialog.Builder, alertTitle: String, alertMessage: String): AlertDialog {
        val view = builder.create().layoutInflater.inflate(R.layout.custom_alertview, null)
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
        dialog.window?.setBackgroundDrawable(ColorDrawable(UIConfig.uiBackgroundSecondaryColor))

        val cancelButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        cancelButton.setTextColor(UIConfig.uiPrimaryColor)

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(UIConfig.uiPrimaryColor)

        setFontType(dialogTitle, MEDIUM)
        setFontType(positiveButton, MEDIUM)
        setFontType(cancelButton, MEDIUM)
        setFontType(dialogMessage, REGULAR)

        return dialog
    }

    override fun customizeRadioButton(button: AppCompatRadioButton) {
        val tintList = ColorStateList(
            arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
            intArrayOf(Color.LTGRAY, UIConfig.uiPrimaryColor)
        )
        button.buttonTintList = tintList
    }

    override fun customizeMenuItem(textView: TextView) {
        setFontType(textView, MEDIUM)
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(UIConfig.textTopBarSecondaryColor)
            setLineSpacing(0f, 1.17f)
        }
    }

    override fun customizeCheckBox(checkBox: AppCompatCheckBox) {
        val tintList = ColorStateList(
            arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
            intArrayOf(Color.LTGRAY, UIConfig.uiPrimaryColor)
        )
        checkBox.buttonTintList = tintList
    }

    override fun customizeEditText(editText: EditText) {
        editText.setTextColor(UIConfig.textPrimaryColor)
        editText.setHintTextColor(ColorUtils.setAlphaComponent(UIConfig.textPrimaryColor, OPACITY_50_PERCENT))
    }

    override fun customizeCardTitle(textView: TextView, enabled: Boolean) {
        setFontType(textView, SEMI_BOLD)
        val color = if (enabled) UIConfig.textPrimaryColor else UIConfig.textSecondaryColor
        textView.setTextColor(color)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
    }

    override fun customizeCardSubtitle(textView: TextView) {
        setFontType(textView, REGULAR)
        textView.setTextColor(UIConfig.textSecondaryColor)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
    }

    override fun customizeCardCta(textView: TextView) {
        setFontType(textView, SEMI_BOLD)
        textView.setTextColor(UIConfig.uiPrimaryColor)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
    }

    override fun customizeAddMoneyEditText(textView: TextView) {
        setFontType(textView, BOLD)
        textView.setTextColor(UIConfig.uiPrimaryColor)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 44f)
    }

    private fun setFontType(textView: TextView, fontType: FontsUtil.FontType) {
        FontsUtil.getFontOfType(fontType)?.let {
            textView.typeface = it
        }
    }

    private fun applyStyle(style: CharacterStyle, spannable: Spannable, start: Int, end: Int) {
        spannable.setSpan(style, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}
