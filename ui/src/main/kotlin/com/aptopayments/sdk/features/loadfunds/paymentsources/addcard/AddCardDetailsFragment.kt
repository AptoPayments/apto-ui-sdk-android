package com.aptopayments.sdk.features.loadfunds.paymentsources.addcard

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.BackButtonMode
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseBindingFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.FragmentAddCardPaymentSourceBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.redmadrobot.inputmask.MaskedTextChangedListener
import kotlinx.android.synthetic.main.fragment_add_card_payment_source.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

private const val CARD_ID_KEY = "CARD_ID_KEY"
private const val DATE_MASK = "[00]/[00]"

internal class AddCardDetailsFragment : BaseBindingFragment<FragmentAddCardPaymentSourceBinding>(),
    AddCardPaymentSourceContract.View {

    override var delegate: AddCardPaymentSourceContract.Delegate? = null
    private val viewModel: AddCardPaymentSourceViewModel by viewModel { parametersOf(cardId) }
    private lateinit var cardId: String
    private lateinit var cardListener: MaskedTextChangedListener

    override fun layoutId() = R.layout.fragment_add_card_payment_source

    override fun backgroundColor() = UIConfig.uiBackgroundSecondaryColor

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
    }

    override fun onPresented() {
        super.onPresented()
        binding.addcardNumberInput.requestFocus()
        showKeyboard()
    }

    override fun setUpArguments() {
        super.setUpArguments()
        cardId = requireArguments()[CARD_ID_KEY] as String
    }

    override fun setupViewModel() {
        observeNotNullable(viewModel.loading) { handleLoading(it) }
        observeNotNullable(viewModel.failure) { handleFailure(it) }
        observeNotNullable(viewModel.cardTransactionCompleted) { onCardTransactionCompleted(it) }
        observeNotNullable(viewModel.cardNumberError) { onCardNumberErrorChanged(it) }
        observeNotNullable(viewModel.expirationError) { onExpirationErrorChanged(it) }
        observeNotNullable(viewModel.creditCardNetwork) { updateCardMask(it) }
    }

    override fun onBackPressed() {
        hideKeyboard()
        delegate?.onBackFromSaveCard()
    }

    override fun setupUI() {
        setDateMask()
        setCardMask()
        setLocalizedHints()
        themeManager().customizeSubmitButton(addcard_continue_button)
        setupToolBar()
    }

    private fun setDateMask() {
        val listener = MaskedTextChangedListener(DATE_MASK, binding.addcardExpirationInput)
        binding.addcardExpirationInput.addTextChangedListener(listener)
        binding.addcardExpirationInput.onFocusChangeListener = listener
    }

    private fun setCardMask() {
        cardListener = MaskedTextChangedListener(CardNetwork.UNKNOWN.visualMask, binding.addcardNumberInput)
        binding.addcardNumberInput.addTextChangedListener(cardListener)
        binding.addcardNumberInput.onFocusChangeListener = cardListener
    }

    private fun updateCardMask(it: CardNetwork) {
        cardListener.primaryFormat = it.visualMask
    }

    private fun setLocalizedHints() {
        styleInputField(
            addcard_number_container,
            addcard_number_input,
            "load_funds_add_card_card_number_placeholder".localized()
        )
        styleInputField(
            addcard_cvv_container,
            addcard_cvv_input,
            "load_funds_add_card_cvv_placeholder".localized()
        )
        styleInputField(
            addcard_expiration_container,
            addcard_expiration_input,
            "load_funds_add_card_date_placeholder".localized()
        )
        styleInputField(
            addcard_zip_container,
            addcard_zip_input,
            "load_funds_add_card_zip_placeholder".localized()
        )
    }

    private fun styleInputField(fieldContainer: TextInputLayout, editText: TextInputEditText, hintString: String) {
        fieldContainer.apply {
            boxStrokeColor = UIConfig.textTertiaryColor
            setErrorTextColor(ColorStateList.valueOf(UIConfig.uiErrorColor))
        }
        editText.hint = hintString
    }

    private fun setupToolBar() {
        tb_llsdk_toolbar.configure(
            this,
            ToolbarConfiguration.Builder()
                .backButtonMode(BackButtonMode.Close(UIConfig.textTopBarSecondaryColor))
                .title("load_funds_add_card_title".localized())
                .setSecondaryColors()
                .build()
        )
    }

    private fun onCardTransactionCompleted(completed: Boolean) {
        if (completed) {
            hideKeyboard()
            delegate?.onCardAdded()
        } else {
            notify("load_funds_add_card_error_title".localized(), "load_funds_add_card_error_message".localized())
        }
    }

    private fun onExpirationErrorChanged(error: Boolean) {
        addcard_expiration_input.error = getExpirationError(error)
    }

    private fun getExpirationError(error: Boolean): CharSequence? {
        return if (error) {
            "load_funds_add_card_expiration_error_invalid".localized()
        } else {
            null
        }
    }

    private fun onCardNumberErrorChanged(error: Boolean) {
        if (error) {
            addcard_number_container.error = "load_funds_add_card_card_number_error_invalid".localized()
        }
        addcard_number_container.isErrorEnabled = error
    }

    companion object {
        fun newInstance(cardId: String, tag: String) = AddCardDetailsFragment().apply {
            arguments = Bundle().apply {
                putSerializable(CARD_ID_KEY, cardId)
            }
            TAG = tag
        }
    }
}
