package com.aptopayments.sdk.features.auth.verification

import android.os.Bundle
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.user.DataPoint
import com.aptopayments.mobile.data.user.EmailDataPoint
import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.mobile.data.user.VerificationStatus
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.MessageBanner
import com.aptopayments.sdk.utils.TextInputWatcher
import com.aptopayments.sdk.utils.ValidInputListener
import com.aptopayments.sdk.utils.shake
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_email_verification.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val VERIFICATION_BUNDLE = "verificationBundle"
private const val PIN_CHARACTERS = 6

internal class EmailVerificationFragment : BaseFragment(), EmailVerificationContract.View {

    override var delegate: EmailVerificationContract.Delegate? = null
    private val viewModel: VerificationViewModel by viewModel()
    private lateinit var verification: Verification
    private lateinit var emailAddress: String

    override fun layoutId() = R.layout.fragment_email_verification

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun setUpArguments() {
        verification = requireArguments()[VERIFICATION_BUNDLE] as Verification
        emailAddress = verification.verificationDataPoint!!
    }

    override fun onPresented() {
        customizePrimaryNavigationStatusBar()
        apto_pin_view?.requestFocus()
        showKeyboard()
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(pinEntryState, ::handlePinEntryState)
            failure(failure) { handleFailure(it) }
        }
        viewModel.verification.postValue(verification)
    }

    override fun setupUI() {
        applyFontsAndColors()
        setupToolBar()
        setupTexts()
    }

    override fun viewLoaded() = viewModel.viewLoaded(DataPoint.Type.EMAIL)

    private fun setupTexts() {
        tv_email_label.text = emailAddress
    }

    private fun applyFontsAndColors() {
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_verification_code_title)
            customizeFormLabel(tv_verification_code_header)
            customizeFormLabel(tv_email_label)
            customizeFooterLabel(tv_resend_label)
            customizeFooterLabel(tv_resend_btn)
            customizeErrorLabel(tv_expired_pin_label)
        }
    }

    private fun setupToolBar() =
        tb_llsdk_toolbar.configure(this, ToolbarConfiguration.Builder().setPrimaryColors().build())

    override fun setupListeners() {
        super.setupListeners()
        val validator = object : ValidInputListener {
            override fun onValidInput(isValid: Boolean) {
                if (isValid) {
                    finishVerification(apto_pin_view.text.toString())
                }
            }
        }
        apto_pin_view.addTextChangedListener(TextInputWatcher(validator, PIN_CHARACTERS, apto_pin_view))
        tv_resend_btn.setOnClickListener { resendVerification() }
    }

    private fun resendVerification() {
        showLoading()
        hideKeyboard()
        viewModel.restartVerification {
            hideLoading()
            notify("auth_verify_email_resent_message".localized(), MessageBanner.MessageType.HEADS_UP)
        }
    }

    private fun finishVerification(pin: String) {
        showLoading()
        viewModel.finishVerification(pin) { result ->
            hideLoading()
            hideKeyboard()
            result.either(::handleFailure) { verification ->
                when (verification.status) {
                    VerificationStatus.PASSED -> {
                        val dataPoint = EmailDataPoint(verification = verification, email = emailAddress)
                        delegate?.onEmailVerificationPassed(dataPoint)
                    }
                    VerificationStatus.FAILED, VerificationStatus.PENDING -> {
                        notify(
                            "auth_verify_email_error_wrong_code_title".localized(),
                            "auth_verify_email_error_wrong_code_message".localized()
                        )
                        apto_pin_view.shake()
                        apto_pin_view.text?.clear()
                        apto_pin_view.clearFocus()
                    }
                }
            }
        }
    }

    private fun handlePinEntryState(newState: PINEntryState?) {
        newState?.let { state ->
            tv_expired_pin_label.goneIf(state is PINEntryState.Enabled)
            apto_pin_view.visibleIf(state is PINEntryState.Enabled)
        }
    }

    override fun onBackPressed() {
        hideKeyboard()
        delegate?.onBackFromEmailVerification()
    }

    companion object {
        fun newInstance(verification: Verification) = EmailVerificationFragment().apply {
            this.arguments = Bundle().apply { putSerializable(VERIFICATION_BUNDLE, verification) }
        }
    }
}
