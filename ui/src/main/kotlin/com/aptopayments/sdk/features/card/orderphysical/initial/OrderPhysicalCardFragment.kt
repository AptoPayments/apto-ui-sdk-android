package com.aptopayments.sdk.features.card.orderphysical.initial

import android.os.Bundle
import android.view.View
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.exception.server.ErrorInsufficientFunds
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.extension.observe
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseBindingFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.FragmentOrderPhysicalCardBinding
import com.aptopayments.sdk.features.card.orderphysical.initial.OrderPhysicalCardViewModel.Action
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.include_toolbar_two.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val CARD_ID = "CARD_ID"

internal class OrderPhysicalCardFragment :
    BaseBindingFragment<FragmentOrderPhysicalCardBinding>(),
    OrderPhysicalCardContract.View {

    private val viewModel: OrderPhysicalCardViewModel by viewModel { parametersOf(cardId) }

    private lateinit var cardId: String

    override var delegate: OrderPhysicalCardContract.Delegate? = null

    override fun setUpArguments() {
        cardId = requireArguments()[CARD_ID] as String
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
    }

    override fun layoutId() = R.layout.fragment_order_physical_card

    override fun backgroundColor() = UIConfig.uiBackgroundSecondaryColor

    override fun setupViewModel() {
        viewModel.apply {
            observe(failure) { handleFailure(it) }
            observeNotNullable(loading) { handleLoading(it) }
            observe(action) {
                when (it) {
                    is Action.ShowSuccessScreen -> delegate?.onCardOrdered()
                    is Action.NavigateToPreviousScreen -> delegate?.onBackFromPhysicalCardOrder()
                }
            }
            observe(state) {
                binding.orderPhysicalCardCard.setCardStyle(it?.cardStyle, it?.cardNetwork)
            }
        }
    }

    override fun setupUI() {
        setupToolBar()
        setupTheme()
    }

    private fun setupToolBar() =
        binding.tbLlsdkToolbarLayout.tb_llsdk_toolbar.configure(
            this,
            ToolbarConfiguration.Builder().setSecondaryColors().build()
        )

    private fun setupTheme() {
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(binding.tbLlsdkToolbarLayout as AppBarLayout)
            customizeLargeTitleLabel(binding.orderPhysicalCardTitle)
            customizeRegularTextLabel(binding.orderPhysicalCardExplanation)
            customizeSubmitButton(binding.orderPhysicalActionOrder)
            customizeMainItemInverted(binding.orderPhysicalCardCardFeeTitle)
            customizeMainItemRightInverted(binding.orderPhysicalCardCardFeeNumber)
        }
    }

    override fun handleFailure(failure: Failure?) {
        when (failure) {
            is ErrorInsufficientFunds ->
                notify(
                    "order_physical_card_order_screen_error_title".localized(),
                    "order_physical_card_order_screen_error_description".localized()
                )
            else -> super.handleFailure(failure)
        }
    }

    override fun onBackPressed() {
        delegate?.onBackFromPhysicalCardOrder()
    }

    companion object {
        fun newInstance(cardId: String, tag: String) = OrderPhysicalCardFragment().apply {
            arguments = Bundle().apply {
                putString(CARD_ID, cardId)
            }
            TAG = tag
        }
    }
}
