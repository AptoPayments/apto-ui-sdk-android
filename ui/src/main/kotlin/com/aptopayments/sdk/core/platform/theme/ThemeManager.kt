package com.aptopayments.sdk.core.platform.theme

import android.graphics.drawable.GradientDrawable
import android.text.Spanned
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.annotation.ColorInt
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.core.graphics.drawable.DrawableCompat
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.loadFromUrl
import com.aptopayments.sdk.utils.FontsUtil.FontType
import com.google.android.material.appbar.AppBarLayout

internal interface ThemeManager {
    fun customizeStatusBar(window: Window)
    fun customizeSecondaryNavigationStatusBar(window: Window)
    fun customizeSecondaryNavigationToolBar(appBarLayout: AppBarLayout)
    fun customizeToolbarTitle(textView: TextView)
    fun customizeHighlightTitleLabel(textView: TextView)
    fun customizeSubmitButton(textView: TextView)
    fun customizeColorlessButton(textView: TextView)
    fun customizeLargeTitleLabel(textView: TextView)
    fun customizeRegularTextLabel(textView: TextView)
    fun customizeRegularTertiaryTextLabel(textView: TextView)
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
    fun customizeSectionOptionTitle(textView: TextView, type: FontType = FontType.MEDIUM)
    fun customizeSectionOptionDescription(textView: TextView)
    fun customizeSectionOptionIcon(imageView: ImageView)
    fun customizeSwitch(switch: Switch)
    fun customizeStarredSectionTitle(textView: TextView, @ColorInt textColor: Int = UIConfig.textTopBarSecondaryColor)
    fun customizeAmountBig(textView: TextView)
    fun customizeAmountMedium(textView: TextView)
    fun customizeAmountSmall(textView: TextView)
    fun customizeSubCurrency(textView: TextView)
    fun customizeEmptyCase(textView: TextView)
    fun customizeBannerTitle(textView: TextView)
    fun customizeBannerMessage(textView: TextView)
    fun getAlertDialog(builder: AlertDialog.Builder, alertTitle: String, alertMessage: String): AlertDialog
    fun customizeRadioButton(button: AppCompatRadioButton)
    fun customizeMenuItem(textView: TextView)
    fun customizeCheckBox(checkBox: AppCompatCheckBox)
    fun customizeEditText(editText: EditText)
    fun customizeCardTitle(textView: TextView, enabled: Boolean = true)
    fun customizeCardSubtitle(textView: TextView)
    fun customizeCardCta(textView: TextView)
    fun customizeAddMoneyEditText(textView: TextView)
    fun customizeMainItemInverted(textView: TextView)
    fun customizeMainItemRightInverted(textView: TextView)

    fun customizeRoundedBackground(view: View) {
        (view.background as? GradientDrawable)?.setColor(UIConfig.uiBackgroundSecondaryColor)
    }

    fun loadLogoOnImageView(imageView: ImageView) {
        imageView.loadFromUrl(UIConfig.logoImage)
    }

    fun customizeMenuImage(item: MenuItem?, @ColorInt color: Int = UIConfig.textTopBarSecondaryColor) =
        item?.icon?.let { icon ->
            DrawableCompat.setTint(icon, color)
        }

    fun customizeMenuLayoutImage(layoutContainer: ViewGroup) {
        layoutContainer.findViewById<ImageView>(R.id.menu_image)?.let {
            DrawableCompat.setTint(it.drawable, UIConfig.iconTertiaryColor)
        }
    }
}

internal fun themeManager(): ThemeManager {
    return ThemeTwoManager
}
