package com.aptopayments.sdk.core.platform.theme

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.Spannable
import android.text.Spanned
import android.text.style.CharacterStyle
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.graphics.drawable.DrawableCompat
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.loadFromUrl
import com.google.android.material.appbar.AppBarLayout

interface ThemeManager {
    fun customizeStatusBar(window: Window)
    fun customizeSecondaryNavigationStatusBar(window: Window)
    fun customizeSecondaryNavigationToolBar(appBarLayout: AppBarLayout)
    fun customizeToolbarTitle(textView: TextView)
    fun customizeHighlightTitleLabel(textView: TextView)
    fun customizeSubmitButton(textView: TextView)
    fun customizeLargeTitleLabel(textView: TextView)
    fun customizeRegularTextLabel(textView: TextView)
    fun customizeSectionHeader(textView: TextView)
    fun customizeFormLabel(textView: TextView)
    fun customizeFooterLabel(textView: TextView)
    fun customizeErrorLabel(textView: TextView)
    fun customizeFormTextLink(textView: TextView)
    fun customizeHtml(textView: TextView, html: Spanned)
    fun customizeContentPlainText(textView: TextView)
    fun customizeContentPlainInvertedText(textView: TextView)
    fun customizeMainItem(textView: TextView)
    fun customizeMainItemRight(textView: TextView)
    fun customizeTimestamp(textView: TextView)
    fun customizeSectionTitle(textView: TextView)
    fun customizeSectionOptionTitle(textView: TextView)
    fun customizeSectionOptionDescription(textView: TextView)
    fun customizeSectionOptionIcon(imageView: ImageView)
    fun customizeSwitch(switch: Switch)
    fun customizeStarredSectionTitle(textView: TextView, @ColorInt textColor: Int = UIConfig.textTopBarSecondaryColor)
    fun customizeAmountBig(textView: TextView)
    fun customizeAmountMedium(textView: TextView)
    fun customizeAmountSmall(textView: TextView)
    fun customizeSubCurrency(textView: TextView)
    fun customizeCardSmallValue(context: Context, textView: TextView)
    fun customizeCardLargeValue(context: Context, textView: TextView)
    fun customizeEmptyCase(textView: TextView)
    fun customizeBannerTitle(textView: TextView)
    fun customizeBannerMessage(textView: TextView)
    fun getAlertDialog(builder: AlertDialog.Builder, alertTitle: String, alertMessage: String): AlertDialog
    fun customizeRadioButton(button: AppCompatRadioButton)
    fun customizeMenuItem(textView: TextView)
    fun customizeCheckBox(checkBox: AppCompatCheckBox)
    fun customizeEditText(editText: EditText)

    fun customizeRoundedBackground(view: View) {
        (view.background as? GradientDrawable)?.setColor(UIConfig.uiBackgroundSecondaryColor)
    }

    fun loadLogoOnImageView(imageView: ImageView) {
        imageView.loadFromUrl(UIConfig.logoImage)
    }

    fun customizeMenuImage(item: MenuItem?, @ColorInt color: Int = UIConfig.textTopBarSecondaryColor) =
        item?.let {
            val icon = it.icon
            icon?.let { icon ->
                DrawableCompat.setTint(icon, color)
                item.icon = icon
            }
        }

    fun customizeMenuLayoutImage(layoutContainer: ViewGroup) {
        layoutContainer.findViewById<ImageView>(R.id.menu_image)?.let {
            DrawableCompat.setTint(it.drawable, UIConfig.iconTertiaryColor)
        }
    }
}

fun themeManager(): ThemeManager {
    return ThemeTwoManager
}

internal fun applyStyle(style: CharacterStyle, spannable: Spannable, start: Int, end: Int) {
    spannable.setSpan(style, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
}
