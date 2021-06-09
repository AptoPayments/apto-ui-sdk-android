package com.aptopayments.sdk.features.loadfunds.add

import android.os.Bundle
import android.view.View
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.BaseBindingFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.FragmentAddFundsBinding
import com.aptopayments.sdk.features.loadfunds.add.AddFundsViewModel.Actions
import com.aptopayments.sdk.ui.views.DigitsInputFilter
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val CARD_ID_KEY = "CARD_ID_KEY"
private const val MAX_INTEGER_DIGITS = 5
private const val MAX_DECIMAL_DIGITS = 2

internal class AddFundsFragment : BaseBindingFragment<FragmentAddFundsBinding>(), AddFundsContract.View {

    override var delegate: AddFundsContract.Delegate? = null
    private val viewModel: AddFundsViewModel by viewModel { parametersOf(cardId) }
    private lateinit var cardId: String
    override fun layoutId() = R.layout.fragment_add_funds
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
        cardId = requireArguments()[CARD_ID_KEY] as String
    }

    override fun setupViewModel() {
        observeNotNullable(viewModel.failure) { handleFailure(it) }
        observeNotNullable(viewModel.loading) { handleLoading(it) }
        observeNotNullable(viewModel.action) { action ->
            hideKeyboard()
            when (action) {
                is Actions.PaymentResult -> delegate?.onPaymentResult(action.payment)
                is Actions.PaymentSourcesList -> delegate?.onPaymentSourcesList()
                is Actions.AddPaymentSource -> delegate?.onAddPaymenSource()
            }
        }
    }

    override fun handleFailure(failure: Failure?) {
        if (failure is Failure.ServerError) {
            notify("failure_server_error".localized())
        } else {
            super.handleFailure(failure)
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
            customizeCardCta(binding.addFundsChangeSource)
            customizeSubmitButton(binding.addFundsButton)
        }
        binding.selectedPaymentSource.setCardBackgroundColor(UIConfig.uiBackgroundSecondaryColor)
        binding.inputPaymentAmountError.setTextColor(UIConfig.textPrimaryColor)
        binding.moneyInput.setTextColor(UIConfig.textPrimaryColor)
    }

    override fun onBackPressed() {
        delegate?.onBackFromAddFunds()
    }

    private fun setupToolBar() {
        tb_llsdk_toolbar.configure(
            this,
            ToolbarConfiguration.Builder()
                .backButtonMode(BackButtonMode.Back(UIConfig.textTopBarSecondaryColor))
                .title("load_funds_add_money_title".localized())
                .setSecondaryColors()
                .build()
        )
    }

    companion object {
        fun newInstance(cardId: String, tag: String) = AddFundsFragment().apply {
            arguments = Bundle().apply {
                putSerializable(CARD_ID_KEY, cardId)
            }
            TAG = tag
        }
    }
}
