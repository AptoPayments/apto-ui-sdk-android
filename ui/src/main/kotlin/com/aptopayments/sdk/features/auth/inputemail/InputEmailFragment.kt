package com.aptopayments.sdk.features.auth.inputemail

import android.text.Editable
import android.text.TextWatcher
import androidx.annotation.VisibleForTesting
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.mobile.data.user.VerificationStatus
import com.aptopayments.mobile.exception.Failure
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.extensions.isValidEmail
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_email_input.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel

@VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
internal class InputEmailFragment : BaseFragment(), InputEmailContract.View {

    override var delegate: InputEmailContract.Delegate? = null
    private val viewModel: InputEmailViewModel by viewModel()

    override fun layoutId() = R.layout.fragment_email_input

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun onPresented() {
        customizePrimaryNavigationStatusBar()
        et_email?.requestFocus()
        showKeyboard()
    }

    override fun setupUI() {
        setupToolBar()
        applyFontsAndColors()
        et_email.hint = "auth_input_email_hint".localized()
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    private fun applyFontsAndColors() {
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_email_header)
            customizeFormLabel(tv_email_label)
            customizeEditText(et_email)
            customizeSubmitButton(continue_button)
        }
    }

    private fun setupToolBar() {
        tb_llsdk_toolbar.configure(this, ToolbarConfiguration.Builder().setPrimaryColors().build())
    }

    override fun setupListeners() {
        super.setupListeners()
        continue_button.setOnClickListener { handleContinueButtonClick() }
        et_email.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?) {
                updateContinueButtonState(s.toString().isValidEmail())
            }
        })
    }

    private fun updateContinueButtonState(enabled: Boolean) {
        continue_button.isEnabled = enabled
    }

    override fun setupViewModel() {
        viewModel.apply {
            observeNotNullable(enableNextButton, ::updateContinueButtonState)
            observeNotNullable(viewModel.loading) { handleLoading(it) }
            observe(verificationData, ::updateVerificationState)
            failure(failure) { handleFailure(it) }
        }
    }

    override fun handleFailure(failure: Failure?) {
        when (failure) {
            is Failure.ServerError -> {
                notify(failure.errorMessage())
            }
            else -> super.handleFailure(failure)
        }
    }

    private fun updateVerificationState(verification: Verification?) {
        verification?.verificationDataPoint = et_email.text.toString()
        verification?.let {
            if (it.status == VerificationStatus.PENDING) {
                hideKeyboard()
                delegate?.onEmailVerificationStarted(verification)
            }
        }
    }

    private fun handleContinueButtonClick() {
        hideKeyboard()
        viewModel.startVerificationUseCase(et_email.text.toString())
    }

    override fun onBackPressed() {
        hideKeyboard()
        delegate?.onBackFromInputEmail()
    }

    companion object {
        fun newInstance() = InputEmailFragment()
    }
}
