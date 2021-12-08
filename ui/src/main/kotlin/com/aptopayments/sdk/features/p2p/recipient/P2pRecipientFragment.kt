package com.aptopayments.sdk.features.p2p.recipient

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.graphics.drawable.DrawableCompat
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.BackButtonMode
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.extension.observe
import com.aptopayments.sdk.core.platform.BaseDataBindingFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.databinding.FragmentP2pRecipientBinding
import com.aptopayments.sdk.features.p2p.recipient.P2pRecipientViewModel.Action
import com.aptopayments.sdk.features.p2p.recipient.P2pRecipientViewModel.Credential.*
import com.aptopayments.sdk.ui.views.PhoneInputView
import com.aptopayments.sdk.utils.FontsUtil
import org.koin.androidx.viewmodel.ext.android.viewModel

internal class P2pRecipientFragment :
    BaseDataBindingFragment<FragmentP2pRecipientBinding>(),
    P2pRecipientContract.View {

    override var delegate: P2pRecipientContract.Delegate? = null
    private val viewModel: P2pRecipientViewModel by viewModel()
    override fun layoutId() = R.layout.fragment_p2p_recipient
    override fun backgroundColor() = UIConfig.uiBackgroundSecondaryColor

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewModel = viewModel
    }

    override fun setupViewModel() {
        observe(viewModel.action) {
            when (it) {
                is Action.Continue -> delegate?.onCardholderSelected(recipient = it.cardholder)
            }
        }
        observe(viewModel.configuration) {
            it?.let {
                setAllowedCountries(it)
                setFocusOnInput(it)
            }
        }
        observe(viewModel.failure, this::handleFailure)
    }

    private fun setAllowedCountries(it: P2pRecipientViewModel.Configuration) {
        if (it.allowedCountries.isNotEmpty()) {
            binding.p2pRecipientPhone.setAllowedCountriesIsoCode(it.allowedCountries)
        }
    }

    private fun setFocusOnInput(it: P2pRecipientViewModel.Configuration) {
        if (it.credential == PHONE) {
            binding.p2pRecipientPhone.requestPhoneFocus()
        } else {
            binding.p2pRecipientEmail.requestFocus()
        }
        showKeyboard()
    }

    override fun setupUI() {
        with(themeManager()) {
            customizeLargeTitleLabel(binding.p2pRecipientTitle)
            customizeLargeSubtitleLabel(binding.p2pRecipientDescription)
            customizeSubmitButton(binding.p2pRecipientContinue)
            DrawableCompat.setTint(binding.p2pLoading.indeterminateDrawable, UIConfig.uiPrimaryColor)
            setFontType(binding.p2pRecipientName, FontsUtil.FontType.REGULAR)
            binding.p2pRecipientName.setTextColor(UIConfig.textSecondaryColor)
            setFontType(binding.p2pRecipientId, FontsUtil.FontType.BOLD)
            binding.p2pRecipientId.setTextColor(UIConfig.textPrimaryColor)
            setFontType(binding.p2pNotFound, FontsUtil.FontType.REGULAR)
            binding.p2pNotFound.setTextColor(UIConfig.textSecondaryColor)
            setFontType(binding.p2pSendSelf, FontsUtil.FontType.REGULAR)
            binding.p2pSendSelf.setTextColor(UIConfig.textSecondaryColor)
        }
        setupToolBar()
        setPhoneChangedListener()
        setEmailChangedListener()
    }

    private fun setPhoneChangedListener() {
        binding.p2pRecipientPhone.delegate = object : PhoneInputView.Delegate {
            override fun onPhoneInputChanged(phoneNumber: String, valid: Boolean) {
                viewModel.onPhoneNumberChanged(phoneNumber, valid)
            }

            override fun onCountryChanged(countryCode: String) {
                // Nothing
            }

            override fun onCountryNumberChanged(countryNumber: String) {
                viewModel.onPhoneCountryChanged(countryNumber)
            }
        }
    }

    private fun setEmailChangedListener() {
        binding.p2pRecipientEmail.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // do nothing
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // do nothing
            }

            override fun afterTextChanged(s: Editable?) {
                viewModel.onEmailChanged(email = s.toString())
            }
        })
    }

    private fun setupToolBar() {
        binding.tbLlsdkToolbarLayout.tbLlsdkToolbar.configure(
            this,
            ToolbarConfiguration.Builder().backButtonMode(BackButtonMode.Close()).setPrimaryColors().build()
        )
    }

    override fun onBackPressed() {
        hideKeyboard()
        delegate?.onTransferRecipientBackPressed()
    }

    companion object {
        fun newInstance(tag: String) = P2pRecipientFragment().apply {
            TAG = tag
        }
    }
}
