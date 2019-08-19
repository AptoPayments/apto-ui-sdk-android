package com.aptopayments.sdk.core.platform.theme

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.StateListDrawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.*
import android.util.TypedValue
import android.view.Gravity
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
import com.aptopayments.sdk.core.extension.setBackgroundColorKeepShape
import com.aptopayments.sdk.core.extension.setColor
import com.aptopayments.sdk.core.ui.StatusBarUtil
import com.aptopayments.sdk.utils.CustomTypefaceSpan
import com.aptopayments.sdk.utils.FontsUtil
import com.aptopayments.sdk.utils.FontsUtil.FontType.REGULAR
import com.aptopayments.sdk.utils.FontsUtil.FontType.SEMI_BOLD
import com.aptopayments.sdk.utils.toDp
import com.google.android.material.appbar.AppBarLayout

private const val CARD_FONT_FILE = "fonts/ocraextended.ttf"

internal object ThemeTwoManager: ThemeManager {
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
        FontsUtil.getFontOfType(FontsUtil.FontType.BOLD)?.let{
            textView.typeface = it
        }
        textView.apply {
            setTextColor(UIConfig.iconTertiaryColor)
            setLineSpacing(0f, 1.17f)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
        }
    }

    override fun customizeHighlightTitleLabel(textView: TextView) {
        FontsUtil.getFontOfType(FontsUtil.FontType.BOLD)?.let{ textView.typeface = it }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
        textView.setTextColor(UIConfig.textSecondaryColor)
    }

    override fun customizeSubmitButton(textView: TextView) {
        FontsUtil.getFontOfType(FontsUtil.FontType.MEDIUM)?.let{ textView.typeface = it }
        textView.apply {
            setTextColor(UIConfig.textButtonColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        }
        val color = UIConfig.uiPrimaryColor
        val cornerRadius = UIConfig.buttonCornerRadius
        val enabledBackground = buttonGradientDrawable(cornerRadius, color)
        val disabledBackground = buttonGradientDrawable(cornerRadius, color, (255 * 0.3).toInt())
        val states = StateListDrawable()
        states.addState(intArrayOf(android.R.attr.state_enabled), enabledBackground)
        states.addState(intArrayOf(-android.R.attr.state_enabled), disabledBackground)
        textView.background = states
    }

    private fun buttonGradientDrawable(cornerRadius: Float, color: Int, alpha: Int = 255): GradientDrawable {
        val disabledBackground = GradientDrawable()
        disabledBackground.cornerRadius = cornerRadius.toDp()
        disabledBackground.setColor(color)
        disabledBackground.alpha = alpha
        return disabledBackground
    }

    override fun customizeLargeTitleLabel(textView: TextView) {
        FontsUtil.getFontOfType(FontsUtil.FontType.BOLD)?.let{ textView.typeface = it }
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 26f)
            setTextColor(UIConfig.textPrimaryColor)
        }
    }

    override fun customizeRegularTextLabel(textView: TextView) {
        FontsUtil.getFontOfType(REGULAR)?.let{ textView.typeface = it }
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(UIConfig.textSecondaryColor)
        }
    }

    override fun customizeSectionHeader(textView: TextView) {
        FontsUtil.getFontOfType(REGULAR)?.let{ textView.typeface = it }
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(UIConfig.textSecondaryColor)
        }
    }

    override fun customizeFormLabel(textView: TextView) {
        FontsUtil.getFontOfType(REGULAR)?.let { textView.typeface = it }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
    }

    override fun customizeFooterLabel(textView: TextView) {
        FontsUtil.getFontOfType(REGULAR)?.let{
            textView.typeface = it
        }
        textView.apply {
            setTextColor(UIConfig.textSecondaryColor)
            setLinkTextColor(UIConfig.textLinkColor)
        }
    }

    override fun customizeErrorLabel(textView: TextView) {
        FontsUtil.getFontOfType(SEMI_BOLD)?.let {
            textView.typeface = it
        }
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        textView.apply {
            setTextColor(UIConfig.uiErrorColor)
        }
    }

    override fun customizeFormTextLink(textView: TextView) {
        FontsUtil.getFontOfType(SEMI_BOLD)?.let {
            textView.typeface = it
        }
        textView.apply {
            setTextColor(UIConfig.textLinkColor)
            setLinkTextColor(UIConfig.textLinkColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            if (UIConfig.underlineLinks) paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }
    }

    override fun customizeHtml(textView: TextView, html: Spanned) {
        FontsUtil.getFontOfType(REGULAR)?.let { textView.typeface = it }
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
                }
                else {
                    applyStyle(CustomTypefaceSpan(typeface), spannable, start, end)
                }
            } ?: applyStyle(StyleSpan(Typeface.BOLD), spannable, start, end)
            if (UIConfig.underlineLinks) applyStyle(UnderlineSpan(), spannable, start, end)
        }
        textView.text = spannable
    }

    override fun customizeContentPlainText(textView: TextView) {
        FontsUtil.getFontOfType(REGULAR)?.let {
            textView.typeface = it
        }
        textView.setTextColor(UIConfig.textPrimaryColor)
    }

    override fun customizeContentPlainInvertedText(textView: TextView) {
        FontsUtil.getFontOfType(REGULAR)?.let {
            textView.typeface = it
        }
        textView.setTextColor(UIConfig.textTopBarSecondaryColor)
    }

    override fun customizeMainItem(textView: TextView) {
        FontsUtil.getFontOfType(FontsUtil.FontType.MEDIUM)?.let {
            textView.typeface = it
        }
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTextColor(UIConfig.textPrimaryColor)
            setLineSpacing(0f, 1.60f)
        }
    }

    override fun customizeMainItemRight(textView: TextView) {
        FontsUtil.getFontOfType(REGULAR)?.let {
            textView.typeface = it
        }
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTextColor(UIConfig.textTertiaryColor)
            setLineSpacing(0f, 1.60f)
            gravity = Gravity.END
        }
    }

    override fun customizeTimestamp(textView: TextView) {
        FontsUtil.getFontOfType(REGULAR)?.let {
            textView.typeface = it
        }
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(UIConfig.textTertiaryColor)
            setLineSpacing(0f, 1.17f)
        }
    }

    override fun customizeSectionTitle(textView: TextView) {
        FontsUtil.getFontOfType(FontsUtil.FontType.MEDIUM)?.let {
            textView.typeface = it
        }
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(UIConfig.textSecondaryColor)
        }
    }

    override fun customizeSectionOptionTitle(textView: TextView) {
        FontsUtil.getFontOfType(FontsUtil.FontType.MEDIUM)?.let{ textView.typeface = it }
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(UIConfig.textSecondaryColor)
        }
    }

    override fun customizeSectionOptionDescription(textView: TextView) {
        FontsUtil.getFontOfType(REGULAR)?.let{ textView.typeface = it }
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
        FontsUtil.getFontOfType(REGULAR)?.let {
            textView.typeface = it
        }
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP,14f)
            alpha = 0.7f
            setTextColor(textColor)
            setAllCaps(true)
            letterSpacing = 0.25f
            setLineSpacing(0f, 1.17f)
        }
    }

    override fun customizeAmountBig(textView: TextView) {
        FontsUtil.getFontOfType(FontsUtil.FontType.BOLD)?.let {
            textView.typeface = it
        }
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 31f)
            setTextColor(UIConfig.textTopBarSecondaryColor)
        }
    }

    override fun customizeAmountMedium(textView: TextView) {
        FontsUtil.getFontOfType(FontsUtil.FontType.MEDIUM)?.let {
            textView.typeface = it
        }
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            setTextColor(UIConfig.textSecondaryColor)
        }
    }

    override fun customizeAmountSmall(textView: TextView) {
        FontsUtil.getFontOfType(FontsUtil.FontType.MEDIUM)?.let { textView.typeface = it }
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTextColor(UIConfig.textPrimaryColor)
        }
    }

    override fun customizeSubCurrency(textView: TextView) {
        FontsUtil.getFontOfType(REGULAR)?.let {
            textView.typeface = it
        }
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP,16f)
            alpha = 0.7f
            setTextColor(UIConfig.textTopBarSecondaryColor)
        }
    }

    override fun customizeCardSmallValue(context: Context, textView: TextView) {
        textView.apply {
            typeface = getCardTypeface(context)
            setTextSize(TypedValue.COMPLEX_UNIT_SP,17f)
            setTextColor(UIConfig.textTopBarSecondaryColor)
        }
    }

    override fun customizeCardLargeValue(context: Context, textView: TextView) {
        textView.apply {
            typeface = getCardTypeface(context)
            setTextSize(TypedValue.COMPLEX_UNIT_SP,24f)
            setTextColor(UIConfig.textTopBarSecondaryColor)
        }
    }

    override fun customizeEmptyCase(textView: TextView) {
        FontsUtil.getFontOfType(FontsUtil.FontType.MEDIUM)?.let {
            textView.typeface = it
        }
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP,14f)
            setTextColor(UIConfig.textTertiaryColor)
            gravity = Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL
        }
    }

    override fun customizeBannerTitle(textView: TextView) {
        FontsUtil.getFontOfType(FontsUtil.FontType.MEDIUM)?.let {
            textView.typeface = it
        }
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            setTextColor(UIConfig.textMessageColor)
        }
    }

    override fun customizeBannerMessage(textView: TextView) {
        FontsUtil.getFontOfType(REGULAR)?.let {
            textView.typeface = it
        }
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setTextColor(UIConfig.textMessageColor)
            maxLines = 100 // Trick to be able to show multiple lines
        }
    }

    override fun getAlertDialog(builder: AlertDialog.Builder, alertTitle: String,
                                alertMessage: String): AlertDialog {
        val view = builder.create().layoutInflater.inflate(R.layout.alertview_theme_two, null)
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

        FontsUtil.getFontOfType(FontsUtil.FontType.MEDIUM)?.let {
            dialogTitle.typeface = it
            positiveButton.typeface = it
            cancelButton.typeface = it
        }
        FontsUtil.getFontOfType(REGULAR)?.let {
            dialogMessage.typeface = it
        }
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
        FontsUtil.getFontOfType(FontsUtil.FontType.MEDIUM)?.let {
            textView.typeface = it
        }
        textView.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(UIConfig.textTopBarSecondaryColor)
            setLineSpacing(0f, 1.17f)
        }
    }

    override fun customizeFormField(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeFormFieldSmall(textView: TextView) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun customizeCheckBox(checkBox: AppCompatCheckBox) {
        val tintList = ColorStateList(
                arrayOf(intArrayOf(-android.R.attr.state_checked), intArrayOf(android.R.attr.state_checked)),
                intArrayOf(Color.LTGRAY, UIConfig.uiPrimaryColor)
        )
        checkBox.buttonTintList = tintList
    }
}
