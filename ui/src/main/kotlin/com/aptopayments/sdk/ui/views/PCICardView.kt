package com.aptopayments.sdk.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.CardBackgroundStyle
import com.aptopayments.mobile.data.card.CardStyle
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.features.managecard.CardInfo
import kotlinx.android.synthetic.main.include_card_disabled_overlay.view.*
import kotlinx.android.synthetic.main.include_card_error_overlay.view.*
import kotlinx.android.synthetic.main.view_pci_card.view.*

private const val CARD_ASPECT_RATIO = 1.586

class PCICardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr, defStyleRes) {

    private lateinit var config: PCIConfiguration
    private var cardStyle: CardStyle? = null
    private var hasValidFundingSource = true
    private var cardState = Card.CardState.ACTIVE
    var delegate: Delegate? = null

    init {
        View.inflate(context, R.layout.view_pci_card, this)
        pci_view.lastFour()
        pci_click_listener.setOnClickListener {
            delegate?.cardViewTapped()
        }
    }

    fun setConfiguration(configuration: PCIConfiguration) {
        this.config = configuration
    }

    private fun initialize(
        config: PCIConfiguration,
        lastFour: String,
        name: String
    ) {
        pci_view.initialise(
            apiKey = config.apiKey,
            userToken = config.token,
            cardId = config.cardId,
            lastFour = lastFour,
            environment = config.environment,
            name = name
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = (width / CARD_ASPECT_RATIO).toInt()
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }

    fun setCardInfo(cardInfo: CardInfo?) {
        setCardStyle(cardInfo?.cardStyle)
        setCardNetwork(cardInfo?.cardNetwork)
        setCardState(cardInfo?.state)
        initialize(config, cardInfo?.lastFourDigits ?: "0000", cardInfo?.cardHolder ?: "")
    }

    fun setCardNetwork(cardNetwork: Card.CardNetwork?) {
        setNetworkImage(cardNetwork)
        updateNetworkLogoIconForCurrentCardStyle()
    }

    private fun setNetworkImage(cardNetwork: Card.CardNetwork?) {
        val res = getLogoForNetwork(cardNetwork)
        res?.let { network_logo.setImageResource(it) }
        network_logo.visibleIf(res != null)
    }

    private fun getLogoForNetwork(cardNetwork: Card.CardNetwork?): Int? {
        return when (cardNetwork) {
            Card.CardNetwork.VISA -> R.drawable.ic_visa_logo
            Card.CardNetwork.MASTERCARD -> R.drawable.ic_mastercard_logo
            else -> null
        }
    }

    private fun setCardStyle(style: CardStyle?) {
        this.cardStyle = style
        setCardBackground(style)
        setCardLogo(style)
        setTextStyles(style)
    }

    private fun setTextStyles(style: CardStyle?) {
        val textColor = style?.textColor?.let { Integer.toHexString(it) } ?: "000000"
        pci_view.alertButtonColor = UIConfig.uiPrimaryColor
        pci_view.styles = mapOf(
            "container" to "width: -webkit-fill-available; height: -webkit-fill-available",
            "content" to mapOf(
                "pan" to "color: #$textColor; font-family: monospace; position: relative; top: 90px; font-size: 26px; text-align: center; font-weight: 600; text-shadow: 0 1px 1px rgba(43, 45, 53, 0.3);",
                "name" to "color: #$textColor; font-family: monospace; position: absolute; bottom: 55px; left: 15px; font-size: 16px; font-weight: 600; text-shadow: 0 1px 1px rgba(43, 45, 53, 0.3);",
                "cvv" to "color: #$textColor; font-family: monospace; position: absolute; bottom: 15px; left: 120px; font-size: 16px; text-shadow: 0 1px 1px rgba(43, 45, 53, 0.3);",
                "exp" to "color: #$textColor; font-family: monospace; position: absolute; bottom: 15px; left: 15px; font-size: 16px; text-shadow: 0 1px 1px rgba(43, 45, 53, 0.3);"
            )
        )
    }

    private fun setCardBackground(style: CardStyle?) {
        when (val backgroundStyle = style?.background) {
            is CardBackgroundStyle.Color -> card_container.setBackgroundColor(backgroundStyle.color)
            is CardBackgroundStyle.Image -> iv_background_image.loadFromUrl(backgroundStyle.url.toString())
            else -> card_container.setBackgroundColor(UIConfig.uiPrimaryColor)
        }
        iv_background_image.visibleIf(style?.background is CardBackgroundStyle.Image)
        updateNetworkLogoIconForCurrentCardStyle()
    }

    private fun setCardLogo(style: CardStyle?) {
        style?.background?.logo.let { card_logo.loadFromUrl(it.toString()) }
        card_logo.visibleIf(style?.background?.logo != null)
    }

    private fun updateNetworkLogoIconForCurrentCardStyle() {
        network_logo.goneIf(cardStyle?.background is CardBackgroundStyle.Image)
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
    }

    private fun showCardDisabled() {
        card_error_overlay.remove()
        card_disabled_overlay.show()
    }

    fun showCardDetails(value: Boolean) {
        if (value) {
            pci_view.reveal()
        } else {
            pci_view.lastFour()
        }
    }

    interface Delegate {
        fun cardViewTapped()
    }
}

data class PCIConfiguration(
    val apiKey: String,
    val environment: String,
    val token: String,
    val cardId: String
)
