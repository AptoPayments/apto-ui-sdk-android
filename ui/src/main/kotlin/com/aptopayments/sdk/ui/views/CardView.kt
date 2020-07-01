package com.aptopayments.sdk.ui.views

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.CardBackgroundStyle
import com.aptopayments.mobile.data.card.CardStyle
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.hide
import com.aptopayments.sdk.core.extension.loadFromUrl
import com.aptopayments.sdk.core.extension.remove
import com.aptopayments.sdk.core.extension.show
import com.aptopayments.sdk.core.platform.theme.themeManager
import kotlinx.android.synthetic.main.include_card_disabled_overlay.view.*
import kotlinx.android.synthetic.main.include_card_error_overlay.view.*
import kotlinx.android.synthetic.main.view_card.view.*
import java.util.Locale

private const val CARD_ASPECT_RATIO = 1.586

class CardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) :
    RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {

    var delegate: Delegate? = null
    private val cardNumberPlaceholder = R.string.manage_card_number_placeholder
    private val expiryDatePlaceholder = R.string.manage_card_expiry_date_placeholder
    private val cvvPlaceholder = R.string.manage_card_cvv_placeholder
    private var cardStyle: CardStyle? = null
    private var lastFour: String? = null
    private var hasValidFundingSource = true
    private var cardState = Card.CardState.ACTIVE
    private var cardNumberElements: List<TextView>

    init {
        View.inflate(context, R.layout.view_card, this)
        cardNumberElements = listOf(card_number_1, card_number_2, card_number_3, card_number_4)
        setupTheme(context)
        setupListeners()
    }

    private fun setupTheme(context: Context) {
        with(themeManager()) {
            cardNumberElements.forEach { customizeCardLargeValue(context, it) }
            customizeCardSmallValue(context, t_card_name)
            customizeCardSmallValue(context, t_expirity_date)
            customizeCardSmallValue(context, t_cvv)
        }
        background.setTint(UIConfig.uiPrimaryColor)
        t_expirity_date.setText(expiryDatePlaceholder)
        t_cvv.setText(cvvPlaceholder)
    }

    private fun setupListeners() {
        setOnClickListener { delegate?.cardViewTapped() }
        cardNumberElements.forEach { it.setOnClickListener { delegate?.panNumberTappedInCardView() } }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = (width / CARD_ASPECT_RATIO).toInt()
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }

    fun setCardholderName(name: String?) {
        t_card_name.text = name?.toUpperCase(Locale.getDefault()) ?: ""
    }

    fun setLastFour(lastFour: String?) {
        this.lastFour = lastFour
        lastFour?.let { card_number_4.text = it } ?: card_number_4.setText(cardNumberPlaceholder)
    }

    fun setPan(pan: String?) {
        if (isValidPan(pan)) {
            setPanNumbers(pan!!)
        } else {
            setLastFourAndPlaceholders()
        }
    }

    private fun isValidPan(pan: String?) = pan != null && pan.length >= 12

    private fun setPanNumbers(it: String) {
        card_number_1.text = it.substring(0, 4)
        card_number_2.text = it.substring(4, 8)
        card_number_3.text = it.substring(8, 12)
        card_number_4.text = it.substring(12)
    }

    private fun setLastFourAndPlaceholders() {
        cardNumberElements.forEach { it.setText(cardNumberPlaceholder) }
        lastFour?.let { card_number_4.text = lastFour }
    }

    @SuppressLint("SetTextI18n")
    fun setExpiryDate(month: String?, year: String?) {
        if (month.isNullOrEmpty() || year.isNullOrEmpty()) {
            t_expirity_date.setText(expiryDatePlaceholder)
        } else {
            t_expirity_date.text = "$month/$year"
        }
    }

    fun setCvv(cvv: String?) {
        cvv?.let { t_cvv.text = it } ?: t_cvv.setText(cvvPlaceholder)
    }

    fun setCardNetwork(cardNetwork: Card.CardNetwork?) {
        when (cardNetwork) {
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
            else -> iv_card_logo.hide()
        }
    }

    fun setCardStyle(style: CardStyle?) {
        this.cardStyle = style
        when (val backgroundStyle = style?.background) {
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
        } else {
            iv_card_logo.show()
        }
    }

    private fun setTextColor(textColor: Int) {
        cardNumberElements.forEach { it.setTextColor(textColor) }
        t_card_name.setTextColor(textColor)
        t_expirity_date.setTextColor(textColor)
        t_cvv.setTextColor(textColor)
        tv_expiration_label.setTextColor(textColor)
        tv_cvv_label.setTextColor(textColor)
    }

    fun setCardState(state: Card.CardState?) {
        cardState = state ?: Card.CardState.INACTIVE
        updateCardState()
    }

    fun setValidFundingSource(value: Boolean) {
        hasValidFundingSource = value
        updateCardState()
    }

    private fun updateCardState() {
        if (cardState == Card.CardState.ACTIVE && hasValidFundingSource) {
            enableCard()
        } else {
            if (!hasValidFundingSource) {
                showInvalidBalance()
            } else {
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
        fun cardViewTapped()
        fun panNumberTappedInCardView()
    }
}
