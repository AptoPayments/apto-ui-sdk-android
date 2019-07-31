package com.aptopayments.sdk.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.aptopayments.sdk.R
import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.card.CardBackgroundStyle
import com.aptopayments.core.data.card.CardStyle
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.fundingsources.Balance
import com.aptopayments.sdk.core.extension.hide
import com.aptopayments.sdk.core.extension.loadFromUrl
import com.aptopayments.sdk.core.extension.remove
import com.aptopayments.sdk.core.extension.show
import com.aptopayments.sdk.core.platform.theme.themeManager
import kotlinx.android.synthetic.main.include_card_disabled_overlay.view.*
import kotlinx.android.synthetic.main.include_card_error_overlay.view.*
import kotlinx.android.synthetic.main.view_card.view.*

private const val CARD_ASPECT_RATIO = 1.586

class CardView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0)
    : RelativeLayout(context, attrs, defStyleAttr, defStyleRes)
{
    var delegate: Delegate? = null
    private val cardNumberPlaceholder = R.string.manage_card_number_placeholder
    private val expiryDatePlaceholder = R.string.manage_card_expiry_date_placeholder
    private val cvvPlaceholder = R.string.manage_card_cvv_placeholder
    private var cardStyle: CardStyle? = null
    private var lastFour: String? = null
    private var hasValidBalance = true
    private var cardState = Card.CardState.ACTIVE

    init {
        View.inflate(context, R.layout.view_card, this)
        setupTheme(context)
        setupListeners()
    }

    private fun setupTheme(context: Context) {
        with(themeManager()) {
            customizeCardLargeValue(context, et_card_number_1)
            customizeCardLargeValue(context, et_card_number_2)
            customizeCardLargeValue(context, et_card_number_3)
            customizeCardLargeValue(context, et_card_number_4)
            customizeCardSmallValue(context, et_card_name)
            customizeCardSmallValue(context, et_expiry_date)
            customizeCardSmallValue(context, et_cvv)
        }
        background.setTint(UIConfig.uiPrimaryColor)
        et_card_number_1.setText(cardNumberPlaceholder)
        et_card_number_2.setText(cardNumberPlaceholder)
        et_card_number_3.setText(cardNumberPlaceholder)
        et_card_number_4.setText(cardNumberPlaceholder)
        et_expiry_date.setText(expiryDatePlaceholder)
        et_cvv.setText(cvvPlaceholder)
    }

    private fun setupListeners() {
        setOnClickListener { delegate?.cardViewTapped(this) }
        et_card_number_1.setOnClickListener { delegate?.panNumberTappedInCardView(this) }
        et_card_number_2.setOnClickListener { delegate?.panNumberTappedInCardView(this) }
        et_card_number_3.setOnClickListener { delegate?.panNumberTappedInCardView(this) }
        et_card_number_4.setOnClickListener { delegate?.panNumberTappedInCardView(this) }
        et_card_name.setOnClickListener { delegate?.cardViewTapped(this) }
        et_expiry_date.setOnClickListener { delegate?.cardViewTapped(this) }
        et_cvv.setOnClickListener { delegate?.cardViewTapped(this) }
        card_disabled_overlay.setOnClickListener { delegate?.cardViewTapped(this) }
        card_error_overlay.setOnClickListener { delegate?.cardViewTapped(this) }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = (width / CARD_ASPECT_RATIO).toInt()
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }

    fun setCardholderName(name: String?) {
        name?.let { et_card_name.setText(it.toUpperCase()) } ?: et_card_name.setText("")
    }

    fun setLastFour(lastFour: String?) {
        this.lastFour = lastFour
        lastFour?.let { et_card_number_4.setText(it) } ?: et_card_number_4.setText(cardNumberPlaceholder)
    }

    fun setPan(pan: String?) {
        pan?.let {
            if (it.length < 12) return
            et_card_number_1.setText(it.substring(0, 4))
            et_card_number_2.setText(it.substring(4, 8))
            et_card_number_3.setText(it.substring(8, 12))
            et_card_number_4.setText(it.substring(12))
        } ?: run {
            et_card_number_1.setText(cardNumberPlaceholder)
            et_card_number_2.setText(cardNumberPlaceholder)
            et_card_number_3.setText(cardNumberPlaceholder)
            lastFour?.let { et_card_number_4.setText(it) } ?: et_card_number_4.setText(cardNumberPlaceholder)
        }
    }

    @SuppressLint("SetTextI18n")
    fun setExpiryDate(month: String?, year: String?) {
        if (month == null || year == null) {
            et_expiry_date.setText(expiryDatePlaceholder)
            return
        }
        et_expiry_date.setText("$month/$year")
    }

    fun setCvv(cvv: String?) {
        cvv?.let { et_cvv.setText(it) } ?: et_cvv.setText(cvvPlaceholder)
    }

    fun setCardNetwork(cardNetwork: Card.CardNetwork?) {
        when(cardNetwork) {
            Card.CardNetwork.VISA -> {
                iv_card_logo.setImageResource(R.drawable.ic_visa_logo)
                iv_card_logo.show()
                updateLogoIconForCurrentCardStyle()
            }
            Card.CardNetwork.MASTERCARD -> {
                iv_card_logo.setImageResource(R.drawable.ic_mastercard_logo)
                iv_card_logo.show()
                updateLogoIconForCurrentCardStyle()
            }
            else -> {
                iv_card_logo.hide()
            }
        }
    }

    fun setCardStyle(style: CardStyle?) {
        this.cardStyle = style
        when(val backgroundStyle = style?.background) {
            is CardBackgroundStyle.Color -> {
                background.setTint(backgroundStyle.color)
                iv_background_image.hide()
            }
            is CardBackgroundStyle.Image -> {
                iv_background_image.loadFromUrl(backgroundStyle.url.toString())
                iv_background_image.show()
            }
            else -> {
                background.setTint(UIConfig.uiPrimaryColor)
                iv_background_image.hide()
            }
        }
        updateLogoIconForCurrentCardStyle()
        style?.textColor?.let {
            setTextColor(it)
        }
    }

    private fun updateLogoIconForCurrentCardStyle() {
        if (cardStyle?.background is CardBackgroundStyle.Image) {
            iv_card_logo.hide()
        }
        else {
            iv_card_logo.show()
        }
    }

    private fun setTextColor(textColor: Int) {
        et_card_number_1.setTextColor(textColor)
        et_card_number_2.setTextColor(textColor)
        et_card_number_3.setTextColor(textColor)
        et_card_number_4.setTextColor(textColor)
        et_card_name.setTextColor(textColor)
        et_expiry_date.setTextColor(textColor)
        et_cvv.setTextColor(textColor)
    }

    fun setCardState(state: Card.CardState?) {
        cardState = state ?: Card.CardState.INACTIVE
        updateCardState()
    }

    fun setBalance(balance: Balance?) {
        hasValidBalance = balance?.state == Balance.BalanceState.VALID
        updateCardState()
    }

    private fun updateCardState() {
        if (cardState == Card.CardState.ACTIVE && hasValidBalance) {
            enableCard()
        } else {
            if (!hasValidBalance) {
                showInvalidBalance()
            }
            else {
                showCardDisabled()
            }
        }
    }

    private fun enableCard() {
        card_disabled_overlay.remove()
        card_error_overlay.remove()
    }

    private fun showInvalidBalance() {
        card_disabled_overlay.remove()
        card_error_overlay.show()
        card_error_overlay.bringToFront()
    }

    private fun showCardDisabled() {
        card_error_overlay.remove()
        card_disabled_overlay.show()
        card_disabled_overlay.bringToFront()
    }

    interface Delegate {
        fun cardViewTapped(cardView: CardView)
        fun panNumberTappedInCardView(cardView: CardView)
    }
}
