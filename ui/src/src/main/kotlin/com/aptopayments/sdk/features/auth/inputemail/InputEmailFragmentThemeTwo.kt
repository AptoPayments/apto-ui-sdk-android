package com.aptopayments.sdk.features.auth.inputemail

import android.annotation.SuppressLint
import android.text.Editable
import android.text.TextWatcher
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.user.Verification
import com.aptopayments.core.data.user.VerificationStatus
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.failure
import com.aptopayments.sdk.core.extension.observe
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.core.ui.State
import com.aptopayments.sdk.utils.StringUtils
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_email_input_theme_two.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel

@VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
internal class InputEmailFragmentThemeTwo : BaseFragment(), InputEmailContract.View {

    override var delegate: InputEmailContract.Delegate? = null
    private val viewModel: InputEmailViewModel by viewModel()

    override fun layoutId() = R.layout.fragment_email_input_theme_two

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun onPresented() {
        delegate?.configureStatusBar()
        et_email.requestFocus()
        showKeyboard()
    }

    @SuppressLint("SetTextI18n")
    override fun setupUI() {
        setupToolBar()
        applyFontsAndColors()
        et_email.hint = "auth_input_email_hint".localized()
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    private fun applyFontsAndColors()   {
        tb_llsdk_toolbar.setTitleTextColor(UIConfig.textTopBarPrimaryColor)
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_email_header)
            customizeFormLabel(tv_email_label)
            customizeEditText(et_email)
            customizeSubmitButton(continue_button)
        }
    }

    private fun setupToolBar() {
        delegate?.configureToolbar(
            toolbar = tb_llsdk_toolbar,
            title = "",
            backButtonMode = BaseActivity.BackButtonMode.Back(null)
        )
    }

    override fun setupListeners() {
        super.setupListeners()
        continue_button.setOnClickListener { handleContinueButtonClick() }
        et_email.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?) {
                updateContinueButtonState(StringUtils.isValidEmail(s.toString()))
            }
        })
    }

    private fun updateContinueButtonState(enabled: Boolean) {
        continue_button.isEnabled = enabled
    }

    override fun setupViewModel() {
        viewModel.apply {
            observeNotNullable(enableNextButton, ::updateContinueButtonState)
            observe(state, ::updateProgressState)
            observe(verificationData, ::updateVerificationState)
            failure(failure) {
                hideLoading()
                handleFailure(it)
            }
        }
    }

    private fun updateVerificationState(verification: Verification?) {
        verification?.verificationDataPoint = et_email.text.toString()
        verification?.let {
            hideLoading()
            if (it.status == VerificationStatus.PENDING) {
                hideKeyboard()
                delegate?.onEmailVerificationStarted(verification)
            }
        }
    }

    private fun updateProgressState(state: State?) {
        if (state == State.IN_PROGRESS) {
            showLoading()
        } else {
            hideLoading()
        }
    }

    private fun handleContinueButtonClick() {
        showLoading()
        hideKeyboard()
        viewModel.startVerificationUseCase(et_email.text.toString())
    }

    override fun onBackPressed() {
        hideKeyboard()
        delegate?.onBackFromInputEmail()
    }

    companion object {
        fun newInstance() = InputEmailFragmentThemeTwo()
    }
}
