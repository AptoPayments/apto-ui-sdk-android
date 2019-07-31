package com.aptopayments.sdk.ui.views

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.aptopayments.sdk.R
import com.aptopayments.core.data.card.Money
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.fundingsources.Balance
import com.aptopayments.sdk.core.extension.interpolateTextSize
import com.aptopayments.sdk.core.extension.loadFromUrl
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.core.platform.theme.themeManager
import kotlinx.android.synthetic.main.view_balance_view_theme_two.view.*
import java.net.URL

class BalanceViewThemeTwo
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr, defStyleRes)
{
    init {
        inflate(context, R.layout.view_balance_view_theme_two, this)
        setupUI()
    }

    private fun setupUI() {
        context?.let { tv_title.text = "manage_card_balance_total_balance".localized(it) }
        setupTheme()
    }

    private fun setupTheme() {
        setBackgroundColor(UIConfig.uiNavigationSecondaryColor)
        with(themeManager()) {
            customizeStarredSectionTitle(tv_title)
            customizeAmountBig(tv_balance)
            customizeSubCurrency(tv_balance_native)
        }
    }

    fun setSelectBalanceIcon(url: URL) {
        iv_refresh.loadFromUrl(url.toString())
    }

    fun set(balance: Balance?) {
        balance?.let {
            if (it.state == Balance.BalanceState.INVALID) {
                showInvalidBalance()
                return
            }
            set(balance = it.balance, nativeBalance = it.custodianWallet?.balance)
        } ?: showInvalidBalance()
    }

    fun applyAlphaAndTextSize(fraction: Float) {
        setBalanceTextSize(fraction)
        setNativeBalanceTextSize(fraction)
        applyAlphaToTitle(fraction)
        applyAlphaToButton(fraction)
        this.isClickable = fraction != 1.0f
    }

    private fun applyAlphaToTitle(fraction: Float) =
            tv_title.setTextColor(ArgbEvaluator().evaluate(fraction, UIConfig.textTopBarColor, Color.TRANSPARENT) as Int)

    private fun applyAlphaToButton(fraction: Float) {
        iv_refresh.imageAlpha = ArgbEvaluator().evaluate(fraction, 255, 0) as Int
    }

    private fun setBalanceTextSize(fraction: Float) =
            tv_balance.interpolateTextSize(23f, 31f, fraction)

    private fun setNativeBalanceTextSize(fraction: Float) =
            tv_balance_native.interpolateTextSize(14f, 16f, fraction)

    private fun showInvalidBalance() = set(balance = null, nativeBalance = null)

    private fun set(balance: Money?, nativeBalance: Money?) {
        tv_balance.text = balance?.toString() ?: invalidBalanceString()
        tv_balance_native.text = nativeBalance?.let { "≈ $it" } ?: ""
    }

    private fun invalidBalanceString(): String {
        return context?.let { "manage_card.balance.invalid_balance.title".localized(it) } ?: ""
    }
}
