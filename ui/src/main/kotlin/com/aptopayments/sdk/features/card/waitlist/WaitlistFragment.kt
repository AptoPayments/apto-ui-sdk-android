package com.aptopayments.sdk.features.card.waitlist

import android.os.Bundle
import com.aptopayments.mobile.data.card.Card
import com.aptopayments.mobile.data.cardproduct.CardProduct
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.content.Content
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.loadFromUrl
import com.aptopayments.sdk.core.extension.observe
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.utils.DarkThemeUtils
import kotlinx.android.synthetic.main.fragment_content_presenter.vw_content_presenter
import kotlinx.android.synthetic.main.fragment_waitlist.*
import kotlinx.android.synthetic.main.view_native_content.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val CARD_ID_PARAMETER_KEY = "card_id"
private const val CARD_PRODUCT_PARAMETER_KEY = "card_product"

internal class WaitlistFragment : BaseFragment(), WaitlistContract.View {

    private val viewModel: WaitlistViewModel by viewModel()
    private lateinit var cardId: String
    private lateinit var cardProduct: CardProduct
    override var delegate: WaitlistContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_waitlist

    override fun backgroundColor(): Int = UIConfig.uiBackgroundSecondaryColor

    override fun setUpArguments() {
        cardId = requireArguments()[CARD_ID_PARAMETER_KEY] as String
        cardProduct = requireArguments()[CARD_PRODUCT_PARAMETER_KEY] as CardProduct
    }

    override fun onResume() {
        super.onResume()
        viewModel.getCard(cardId)
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(card) { handleCard(it) }
        }
    }

    override fun setupUI() {
        showWaitlist()
        setupTexts()
    }

    private fun setupTexts() {
        tv_native_content_title.localizedText = "wait_list.wait_list.title"
        tv_native_content_description_main.localizedText = "wait_list.wait_list.description.main"
        tv_native_content_description_secondary.localizedText = "wait_list.wait_list.description.secondary"
    }

    private fun showWaitlist() {
        val content = Content.Native(
            backgroundColor = getBackgroundColor(),
            backgroundImage = cardProduct.waitlistBackgroundImage,
            asset = cardProduct.waitlistAsset
        )
        vw_content_presenter.content = content
        content.backgroundImage?.let { iv_background.loadFromUrl(it.toString()) }
    }

    private fun getBackgroundColor(): Int? {
        return context?.let {
            val darkThemeUtils = DarkThemeUtils(AptoUiSdk, it)
            if (darkThemeUtils.isEnabled()) cardProduct.waitlistDarkBackgroundColor else cardProduct.waitlistBackgroundColor
        } ?: cardProduct.waitlistBackgroundColor
    }

    private fun handleCard(card: Card?) {
        if (card?.isWaitlisted == false) delegate?.onWaitlistFinished()
    }

    companion object {
        fun newInstance(cardProduct: CardProduct, cardId: String): WaitlistFragment =
            WaitlistFragment().apply {
                arguments = Bundle().apply {
                    putString(CARD_ID_PARAMETER_KEY, cardId)
                    putSerializable(CARD_PRODUCT_PARAMETER_KEY, cardProduct)
                }
            }
    }
}
