package com.aptopayments.sdk.features.card.passcode.start

import android.os.Bundle
import android.view.View
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.BackButtonMode
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseDataBindingFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.FragmentCardPasscodeStartBinding
import com.aptopayments.sdk.features.card.passcode.start.CardPasscodeStartViewModel.Action

import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val CARD_ID = "CARD_ID"

internal class CardPasscodeStartFragment :
    BaseDataBindingFragment<FragmentCardPasscodeStartBinding>(),
    CardPasscodeStartContract.View {

    override var delegate: CardPasscodeStartContract.Delegate? = null
    private val viewModel: CardPasscodeStartViewModel by viewModel { parametersOf(cardId) }
    private lateinit var cardId: String

    override fun layoutId(): Int = R.layout.fragment_card_passcode_start

    override fun backgroundColor() = UIConfig.uiBackgroundSecondaryColor

    override fun setUpArguments() {
        cardId = requireArguments()[CARD_ID] as String
    }

    override fun setupViewModel() {
        observeNotNullable(viewModel.actions) { onActionPerformed(it) }
        observeNotNullable(viewModel.loading) { handleLoading(it) }
        observeNotNullable(viewModel.failure) { handleFailure(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
    }

    private fun onActionPerformed(action: Action?) {
        when (action) {
            is Action.StartedWithVerification -> delegate?.onStartedWithVerification(action.verification)
            is Action.StartedWithoutVerification -> delegate?.onStartedWithoutVerification()
            is Action.Cancel -> delegate?.onBackFromPasscodeStart()
        }
    }

    override fun setupUI() {
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(binding.tbLlsdkToolbarLayout as AppBarLayout)
            customizeLargeTitleLabel(binding.cardPasscodeStartTitle)
            customizeFormLabel(binding.cardPasscodeStartDescription)
            customizeSubmitButton(binding.cardPasscodeStartContinue)
            customizeColorlessButton(binding.cardPasscodeStartCancel)
        }
        setupToolBar()
    }

    private fun setupToolBar() {
        binding.tbLlsdkToolbarLayout.tbLlsdkToolbar.configure(
            this,
            ToolbarConfiguration.Builder()
                .backButtonMode(BackButtonMode.Close(UIConfig.textTopBarSecondaryColor))
                .setSecondaryColors()
                .build()
        )
    }

    override fun onBackPressed() {
        delegate?.onBackFromPasscodeStart()
    }

    companion object {
        fun newInstance(cardId: String, tag: String) = CardPasscodeStartFragment().apply {
            TAG = tag
            arguments = Bundle().apply {
                putString(CARD_ID, cardId)
            }
        }
    }
}
