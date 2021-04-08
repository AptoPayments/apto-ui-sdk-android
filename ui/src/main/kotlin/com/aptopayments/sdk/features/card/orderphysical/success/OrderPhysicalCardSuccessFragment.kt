package com.aptopayments.sdk.features.card.orderphysical.success

import android.os.Bundle
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.features.card.orderphysical.success.OrderPhysicalCardSuccessViewModel.Action
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_activate_physical_card_success.tb_llsdk_toolbar_layout
import kotlinx.android.synthetic.main.fragment_order_physical_success.*
import kotlinx.android.synthetic.main.include_toolbar_two.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val CARD_ID = "CARD_ID"

internal class OrderPhysicalCardSuccessFragment : BaseFragment(), OrderPhysicalCardSuccessContract.View {

    private lateinit var cardId: String
    private val viewModel: OrderPhysicalCardSuccessViewModel by viewModel { parametersOf(cardId) }

    override var delegate: OrderPhysicalCardSuccessContract.Delegate? = null

    override fun setUpArguments() {
        cardId = requireArguments()[CARD_ID] as String
    }

    override fun layoutId() = R.layout.fragment_order_physical_success

    override fun backgroundColor() = UIConfig.uiBackgroundSecondaryColor

    override fun setupViewModel() {
        observe(viewModel.state) { order_physical_card_card.setCardStyle(it?.cardStyle, it?.cardNetwork) }
        observe(viewModel.action) {
            when (it) {
                is Action.OrderPhysicalDone -> delegate?.onBackFromPhysicalCardSuccess()
            }
        }
    }

    override fun setupUI() {
        setupToolBar()
        order_physical_success_done.setOnClickListenerSafe { viewModel.onDone() }
        setupTheme()
    }

    private fun setupToolBar() =
        tb_llsdk_toolbar_layout.tb_llsdk_toolbar.configure(
            this,
            ToolbarConfiguration.Builder()
                .backButtonMode(BackButtonMode.None)
                .setSecondaryColors().build()
        )

    private fun setupTheme() {
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(order_physical_card_success_title)
            customizeRegularTextLabel(order_physical_card_success_explanation)
            customizeSubmitButton(order_physical_success_done)
        }
    }

    override fun onBackPressed() {
        viewModel.onDone()
    }

    companion object {
        fun newInstance(cardId: String, tag: String) = OrderPhysicalCardSuccessFragment().apply {
            arguments = Bundle().apply {
                putString(CARD_ID, cardId)
            }
            TAG = tag
        }
    }
}
