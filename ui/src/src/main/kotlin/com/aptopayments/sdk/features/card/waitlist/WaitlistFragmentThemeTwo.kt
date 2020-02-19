package com.aptopayments.sdk.features.card.waitlist

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.card.Card
import com.aptopayments.core.data.cardproduct.CardProduct
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.content.Content
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.loadFromUrl
import com.aptopayments.sdk.core.extension.observe
import com.aptopayments.sdk.core.platform.AptoUiSdk
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.utils.DarkThemeUtils
import kotlinx.android.synthetic.main.fragment_content_presenter_theme_two.vw_content_presenter
import kotlinx.android.synthetic.main.fragment_waitlist_theme_two.*
import kotlinx.android.synthetic.main.view_native_content.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.reflect.Modifier

private const val CARD_ID_PARAMETER_KEY = "card_id"
private const val CARD_PRODUCT_PARAMETER_KEY = "card_product"
@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class WaitlistFragmentThemeTwo : BaseFragment(), WaitlistContract.View {

    private val viewModel: WaitlistViewModel by viewModel()
    private lateinit var cardId: String
    private lateinit var cardProduct: CardProduct
    override var delegate: WaitlistContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_waitlist_theme_two

    override fun backgroundColor(): Int = UIConfig.uiBackgroundSecondaryColor

    override fun setUpArguments() {
        cardId = arguments!![CARD_ID_PARAMETER_KEY] as String
        cardProduct = arguments!![CARD_PRODUCT_PARAMETER_KEY] as CardProduct
    }

    override fun onResume() {
        super.onResume()
        viewModel.getCard(cardId)
    }

    override fun viewLoaded() {
        super.viewLoaded()
        viewModel.viewLoaded()
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

    @SuppressLint("SetTextI18n")
    private fun setupTexts() {
        tv_native_content_title.text = "wait_list.wait_list.title".localized()
        tv_native_content_description_main.text = "wait_list.wait_list.description.main".localized()
        tv_native_content_description_secondary.text = "wait_list.wait_list.description.secondary".localized()
    }

    private fun showWaitlist() {
        val content = Content.Native(
                backgroundColor = getBackgroundColor(),
                backgroundImage = cardProduct.waitlistBackgroundImage,
                asset = cardProduct.waitlistAsset)
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
        fun newInstance(cardProduct: CardProduct, cardId: String): WaitlistFragmentThemeTwo =
                WaitlistFragmentThemeTwo().apply {
                    arguments = Bundle().apply {
                        putString(CARD_ID_PARAMETER_KEY, cardId)
                        putSerializable(CARD_PRODUCT_PARAMETER_KEY, cardProduct)
                    }
                }
    }
}

