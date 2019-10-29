package com.aptopayments.sdk.features.auth.verification

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.user.DataPoint
import com.aptopayments.core.data.user.EmailDataPoint
import com.aptopayments.core.data.user.Verification
import com.aptopayments.core.data.user.VerificationStatus
import com.aptopayments.core.extension.localized
import com.aptopayments.core.extension.stringFromTimeInterval
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.failure
import com.aptopayments.sdk.core.extension.hide
import com.aptopayments.sdk.core.extension.observe
import com.aptopayments.sdk.core.extension.show
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.MessageBanner
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_email_verification_theme_one.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.reflect.Modifier

private const val VERIFICATION_BUNDLE = "verificationBundle"

internal class EmailVerificationFragmentThemeOne : BaseFragment(), EmailVerificationContract.View {

    override var delegate: EmailVerificationContract.Delegate? = null
    @VisibleForTesting(otherwise = Modifier.PRIVATE) val viewModel: VerificationViewModel by viewModel()
    @VisibleForTesting(otherwise = Modifier.PRIVATE) lateinit var verification: Verification
    @VisibleForTesting(otherwise = Modifier.PRIVATE) lateinit var emailAddress: String

    override fun layoutId() = R.layout.fragment_email_verification_theme_one

    override fun setUpArguments() {
        verification = arguments!![VERIFICATION_BUNDLE] as Verification
        emailAddress = verification.verificationDataPoint!!
    }

    override fun onPresented() {
        delegate?.configureStatusBar()
        apto_pin_view?.requestFocus()
        showKeyboard()
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(pinEntryState, ::handlePinEntryState)
            observe(resendButtonState, ::handleResendButtonState)
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

    @SuppressLint("SetTextI18n")
    private fun setupTexts() {
        context?.let {
            tv_verification_code_title.text = "auth.verify_email.explanation".localized(it)
            tv_resend_label.text = "auth.verify_email.footer".localized(it)
            tv_resend_btn.text = "auth.verify_email.resend_button.title".localized(it)
            tv_expired_pin_label.text = "auth.verify_email.expired_pin.text".localized(it)
        }
        tv_email_label.text = emailAddress
    }

    private fun applyFontsAndColors() {
        view?.setBackgroundColor(UIConfig.uiBackgroundPrimaryColor)
        tb_llsdk_toolbar.setTitleTextColor(UIConfig.textTopBarPrimaryColor)
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeFormLabel(tv_verification_code_title)
            customizeFormFieldSmall(tv_email_label)
            customizeFormFieldSmall(tv_resend_label)
            customizeFormFieldSmall(tv_expired_pin_label)
            customizeFormFieldSmall(tv_resend_countdown_label)
        }
    }

    private fun setupToolBar() = delegate?.configureToolbar(
            toolbar = tb_llsdk_toolbar,
            title = activity?.let { "auth_verify_email_title".localized(it) },
            backButtonMode = BaseActivity.BackButtonMode.Close(null, UIConfig.iconTertiaryColor)
    )

    override fun setupListeners() {
        super.setupListeners()
        apto_pin_view.setOnCompleteListener { completed, _ ->
            if (completed) finishVerification()
        }
        tv_resend_btn.setOnClickListener { resendVerification() }
    }

    private fun resendVerification() {
        showLoading()
        hideKeyboard()
        viewModel.restartVerification {
            hideLoading()
            context?.let { context ->
                notify("auth_verify_email_resent_message".localized(context), MessageBanner.MessageType.HEADS_UP)
            }
        }
    }

    private fun finishVerification() {
        showLoading()
        viewModel.finishVerification(apto_pin_view.pinResults) { result ->
            hideLoading()
            hideKeyboard()
            result.either(::handleFailure) { verification ->
                hideKeyboard()
                when (verification.status) {
                    VerificationStatus.PASSED -> {
                        val dataPoint = EmailDataPoint(
                                verification = verification,
                                email = emailAddress
                        )
                        delegate?.onEmailVerificationPassed(dataPoint)
                    }
                    VerificationStatus.FAILED, VerificationStatus.PENDING -> {
                        hideLoading()
                        context?.let {
                            notify("auth.verify_email.error_wrong_code.title".localized(it), "auth.verify_email.error_wrong_code.message".localized(it))
                            apto_pin_view.clear()
                            apto_pin_view.clearFocus()
                        }
                        hideKeyboard()
                    }
                }
                Unit
            }
        }
    }

    private fun handlePinEntryState(newState: PINEntryState?) {
        newState?.let { state ->
            when (state) {
                is PINEntryState.Enabled -> {
                    tv_expired_pin_label.hide()
                    apto_pin_view.show()
                }
                is PINEntryState.Expired -> {
                    tv_expired_pin_label.show()
                    apto_pin_view.hide()
                }
            }
        }
    }

    private fun handleResendButtonState(newState: ResendButtonState?) {
        newState?.let { state ->
            when (state) {
                is ResendButtonState.Enabled -> {
                    tv_resend_btn.show()
                    tv_resend_countdown_label.hide()
                }
                is ResendButtonState.Waiting -> {
                    context?.let {
                        val waitTimeDescription = state.pendingSeconds.stringFromTimeInterval()
                        val newText = "auth.verify_email.resent_wait_text".localized(it).replace("<<WAIT_TIME>>", waitTimeDescription)
                        tv_resend_countdown_label.text = newText
                    }
                    tv_resend_btn.hide()
                    tv_resend_countdown_label.show()
                }
            }
        }
    }

    override fun onBackPressed() {
        hideKeyboard()
        delegate?.onBackFromEmailVerification()
    }

    companion object {
        fun newInstance(verification: Verification) = EmailVerificationFragmentThemeOne().apply {
            this.arguments = Bundle().apply { putSerializable(VERIFICATION_BUNDLE, verification) }
        }
    }
}
