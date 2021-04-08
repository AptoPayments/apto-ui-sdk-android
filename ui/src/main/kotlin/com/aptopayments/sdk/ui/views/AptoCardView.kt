package com.aptopayments.sdk.ui.views

import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.card.CardBackgroundStyle
import com.aptopayments.mobile.data.card.CardStyle
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.databinding.ViewCardBinding

private const val CARD_ASPECT_RATIO = 1.586

class AptoCardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr) {

    private var binding = ViewCardBinding.inflate(LayoutInflater.from(context), this, true)

    fun setCardStyle(cardStyle: CardStyle?, cardNetwork: Card.CardNetwork?) {
        setCardBackground(cardStyle)
        setNetworkImage(cardStyle, cardNetwork)
        setCompanyLogo(cardStyle)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = (width / CARD_ASPECT_RATIO).toInt()
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }

    private fun setNetworkImage(cardStyle: CardStyle?, cardNetwork: Card.CardNetwork?) {
        val res = getLogoForNetwork(cardNetwork)
        res?.let { binding.cardViewNetworkLogo.setImageResource(it) }
        binding.cardViewNetworkLogo.visibleIf(res != null && cardStyle?.background !is CardBackgroundStyle.Image)
    }

    private fun getLogoForNetwork(cardNetwork: Card.CardNetwork?): Int? {
        return when (cardNetwork) {
            Card.CardNetwork.VISA -> R.drawable.ic_visa_logo
            Card.CardNetwork.MASTERCARD -> R.drawable.ic_mastercard_logo
            else -> null
        }
    }

    private fun setCardBackground(style: CardStyle?) {
        when (val backgroundStyle = style?.background) {
            is CardBackgroundStyle.Color -> setCardColor(backgroundStyle.color)
            is CardBackgroundStyle.Image -> binding.cardViewBackgroundImage.loadFromUrl(backgroundStyle.url.toString())
            else -> setCardColor(UIConfig.uiPrimaryColor)
        }
    }

    private fun setCardColor(color: Int) {
        binding.cardViewBackgroundImage.setImageDrawable(ColorDrawable(color))
    }

    private fun setCompanyLogo(style: CardStyle?) {
        style?.background?.logo.let { binding.cardViewCardLogo.loadFromUrl(it.toString()) }
        binding.cardViewCardLogo.visibleIf(style?.background?.logo != null)
    }
}
