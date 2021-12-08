package com.aptopayments.sdk.features.p2p.funds

import android.os.Bundle
import android.view.View
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.transfermoney.CardHolderData
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.BackButtonMode
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseDataBindingFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.FragmentP2pSendFundsBinding
import com.aptopayments.sdk.features.p2p.funds.SendFundsViewModel.Action.*
import com.aptopayments.sdk.ui.views.DigitsInputFilter
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val CARDHOLDER_DATA = "CARDHOLDER_DATA_KEY"
private const val CARDID_DATA = "CARDID_DATA_KEY"

private const val MAX_INTEGER_DIGITS = 5
private const val MAX_DECIMAL_DIGITS = 2

internal class SendFundsFragment : BaseDataBindingFragment<FragmentP2pSendFundsBinding>(), SendFundsContract.View {

    override var delegate: SendFundsContract.Delegate? = null
    private lateinit var cardId: String
    private lateinit var recipient: CardHolderData

    private val viewModel: SendFundsViewModel by viewModel { parametersOf(cardId, recipient) }

    override fun layoutId() = R.layout.fragment_p2p_send_funds

    override fun backgroundColor() = UIConfig.uiBackgroundSecondaryColor

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
    }

    override fun onPresented() {
        super.onPresented()
        binding.moneyInput.requestFocus()
        showKeyboard()
    }

    override fun onPause() {
        super.onPause()
        hideKeyboard()
    }

    override fun setUpArguments() {
        super.setUpArguments()
        cardId = requireArguments()[CARDID_DATA] as String
        recipient = requireArguments()[CARDHOLDER_DATA] as CardHolderData
    }

    override fun setupViewModel() {
        observeNotNullable(viewModel.failure) { handleFailure(it) }
        observeNotNullable(viewModel.loading) { handleLoading(it) }
        observeNotNullable(viewModel.action) {
            when (it) {
                is ChangeRecipient -> delegate?.onChangeRecipient()
                is PaymentSuccess -> delegate?.onPaymentSuccess(it.payment)
                is PaymentFailure -> {
                    hideKeyboard()
                    handleFailure(object : Failure.FeatureFailure(
                        errorKey = "p2p_transfer_send_funds_transfer_error",
                        titleKey = "banner_error_title"
                    ) {})
                }
            }
        }
    }

    override fun setupUI() {
        setupToolBar()
        setUpViews()
        binding.moneyInput.filters = arrayOf(DigitsInputFilter(MAX_INTEGER_DIGITS, MAX_DECIMAL_DIGITS))
    }

    private fun setUpViews() {
        with(themeManager()) {
            customizeAddMoneyEditText(binding.moneyInput)
            customizeCardCta(binding.p2pSendChange)
            customizeSubmitButton(binding.p2pSendCta)
        }
        binding.p2pSendSelectedRecipient.setCardBackgroundColor(UIConfig.uiBackgroundSecondaryColor)
        binding.inputPaymentAmountError.setTextColor(UIConfig.textPrimaryColor)
        binding.moneyInput.setTextColor(UIConfig.textPrimaryColor)
    }

    override fun onBackPressed() {
        delegate?.onBackFromSendFunds()
    }

    private fun setupToolBar() {
        binding.tbLlsdkToolbarLayout.tbLlsdkToolbar.configure(
            this,
            ToolbarConfiguration.Builder()
                .backButtonMode(BackButtonMode.Back(UIConfig.textTopBarSecondaryColor))
                .title("p2p_transfer_send_funds_screen_title".localized())
                .setSecondaryColors()
                .build()
        )
    }

    companion object {
        fun newInstance(cardId: String, recipient: CardHolderData, tag: String) = SendFundsFragment().apply {
            arguments = Bundle().apply {
                putSerializable(CARDID_DATA, cardId)
                putSerializable(CARDHOLDER_DATA, recipient)
            }
            TAG = tag
        }
    }
}
