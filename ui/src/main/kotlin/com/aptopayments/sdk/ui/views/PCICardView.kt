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
import com.aptopayments.sdk.pci.config.*
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe
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
        pci_view.hidePCIData()
        pci_click_listener.setOnClickListenerSafe {
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
        val configAuth = PCIConfigAuth(
            apiKey = config.apiKey,
            userToken = config.token,
            cardId = config.cardId,
            environment = PCIEnvironment.valueOf(config.environment.toUpperCase())
        )
        val configCard = PCIConfigCard(lastFour = lastFour, nameOnCard = name)

        pci_view.init(PCIConfig(configAuth = configAuth, configCard = configCard))
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

    private fun setCardNetwork(cardNetwork: Card.CardNetwork?) {
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
        pci_view.setStyle(PCIConfigStyle(textColor = style?.textColor, alertButtonColor = UIConfig.uiPrimaryColor))
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
            pci_view.showPCIData()
        } else {
            pci_view.hidePCIData()
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
