package com.aptopayments.sdk.features.directdeposit.instructions

import android.os.Bundle
import android.view.View
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.BackButtonMode
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseDataBindingFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.FragmentDirectDepositInstructionsBinding
import com.aptopayments.sdk.features.directdeposit.instructions.DirectDepositInstructionsViewModel.Actions
import com.aptopayments.sdk.utils.extensions.SnackbarMessageType
import com.aptopayments.sdk.utils.extensions.copyToClipboard
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val CARD_ID = "CARD_ID"

internal class DirectDepositInstructionsFragment :
    BaseDataBindingFragment<FragmentDirectDepositInstructionsBinding>(),
    DirectDepositInstructionsContract.View {

    private val viewModel: DirectDepositInstructionsViewModel by viewModel { parametersOf(cardId) }

    override var delegate: DirectDepositInstructionsContract.Delegate? = null

    private lateinit var cardId: String

    override fun layoutId() = R.layout.fragment_direct_deposit_instructions

    override fun backgroundColor() = UIConfig.uiBackgroundSecondaryColor

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
    }

    override fun setupViewModel() {
        observeNotNullable(viewModel.loading) { handleLoading(it) }
        observeNotNullable(viewModel.failure) { handleFailure(it) }
        observeNotNullable(viewModel.actions) {
            when (it) {
                is Actions.CopyToClipboard -> copyValue(it.label, it.value)
            }
        }
    }

    private fun copyValue(label: String, value: String) {
        requireContext().copyToClipboard(label = label, value = value)
        notify(
            "load_funds_direct_deposit_instructions_copied_to_clipboard".localized(),
            SnackbarMessageType.HEADS_UP
        )
    }

    override fun setUpArguments() {
        super.setUpArguments()
        cardId = requireArguments()[CARD_ID] as String
    }

    override fun setupUI() {
        setUpToolbar()
        with(themeManager()) {
            customizeMainItem(binding.directDepositInstructionsNameTitle)
            customizeMainItem(binding.directDepositInstructionsAccountNumberTitle)
            customizeMainItem(binding.directDepositInstructionsRoutingNumberTitle)
            customizeMainItemRight(binding.directDepositInstructionsNameInfo)
            customizeMainItemRight(binding.directDepositInstructionsAccountNumberInfo)
            customizeMainItemRight(binding.directDepositInstructionsRoutingNumberInfo)
            customizeRegularTextLabel(binding.directDepositInstructionsDescription)
            customizeRegularTertiaryTextLabel(binding.directDepositInstructionsLowerDescription)
        }
        setLongClickListener(binding.directDepositInstructionsRoutingContainer) { viewModel.onRoutingNumberCopy() }
        setLongClickListener(binding.directDepositInstructionsAccountContainer) { viewModel.onAccountNumberCopy() }
    }

    private fun setLongClickListener(v: View, lambda: () -> Unit) {
        v.setOnLongClickListener {
            lambda.invoke()
            true
        }
    }

    private fun setUpToolbar() {
        binding.tbLlsdkToolbarLayout.tbLlsdkToolbar.configure(
            this,
            ToolbarConfiguration.Builder()
                .backButtonMode(BackButtonMode.Back(UIConfig.textTopBarSecondaryColor))
                .setSecondaryColors()
                .title("load_funds_direct_deposit_instructions_title".localized())
                .build()
        )
    }

    override fun onBackPressed() {
        delegate?.onBackFromDirectDepositInstructions()
    }

    companion object {
        fun newInstance(cardId: String, tag: String) = DirectDepositInstructionsFragment().apply {
            arguments = Bundle().apply {
                putSerializable(CARD_ID, cardId)
            }
            TAG = tag
        }
    }
}
