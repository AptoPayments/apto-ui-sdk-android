package com.aptopayments.sdk.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.CardStyle
import com.aptopayments.sdk.databinding.ViewPciCardBinding
import com.aptopayments.sdk.features.managecard.CardInfo
import com.aptopayments.sdk.pci.config.*

private const val CARD_ASPECT_RATIO = 1.586

class PCICardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private lateinit var config: PCIConfiguration
    private var hasValidFundingSource = true
    private var cardState = Card.CardState.ACTIVE

    private var binding = ViewPciCardBinding.inflate(LayoutInflater.from(context), this, true)

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
            environment = PCIEnvironment.valueOf(config.environment.uppercase())
        )
        val configCard = PCIConfigCard(lastFour = lastFour, nameOnCard = name)

        binding.pciView.init(PCIConfig(configAuth = configAuth, configCard = configCard))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = (width / CARD_ASPECT_RATIO).toInt()
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }

    fun setCardInfo(cardInfo: CardInfo?) {
        initialize(config, cardInfo?.lastFourDigits ?: "0000", cardInfo?.cardHolder ?: "")
        setCardStyle(cardInfo)
        setCardState(cardInfo?.state)
    }

    private fun setCardStyle(cardInfo: CardInfo?) {
        binding.pciCardCardView.setCardStyle(
            cardInfo?.cardStyle,
            cardInfo?.cardNetwork
        )
        setTextStyles(cardInfo?.cardStyle)
    }

    private fun setTextStyles(style: CardStyle?) {
        binding.pciView.setStyle(
            PCIConfigStyle(
                textColor = style?.textColor,
            ),
            theme = null
        )
    }

    private fun setCardState(state: Card.CardState?) {
        cardState = state ?: Card.CardState.INACTIVE
        updateCardState()
    }

    fun setValidFundingSource(value: Boolean) {
        hasValidFundingSource = value
        updateCardState()
    }

    private fun updateCardState() {
        setCardStatus(getCardStatus())
    }

    private fun getCardStatus(): Status {
        return when {
            cardState == Card.CardState.ACTIVE && hasValidFundingSource -> Status.CARD_ENABLED
            !hasValidFundingSource -> Status.INVALID_BALANCE
            else -> Status.CARD_DISABLED
        }
    }

    private fun setCardStatus(status: Status) {
        binding.status = status
        binding.executePendingBindings()
    }

    fun showCardDetails(value: Boolean) {
        if (value) {
            binding.pciView.showPCIData()
        } else {
            binding.pciView.hidePCIData()
        }
    }

    enum class Status {
        CARD_ENABLED, INVALID_BALANCE, CARD_DISABLED
    }
}

data class PCIConfiguration(
    val apiKey: String,
    val environment: String,
    val token: String,
    val cardId: String
)
