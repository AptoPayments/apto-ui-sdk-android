package com.aptopayments.sdk.features.auth.verification

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.user.DataPoint
import com.aptopayments.core.data.user.PhoneDataPoint
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
import com.aptopayments.sdk.utils.StringUtils
import com.google.android.material.appbar.AppBarLayout
import com.google.i18n.phonenumbers.NumberParseException
import kotlinx.android.synthetic.main.fragment_phone_verification_theme_one.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.reflect.Modifier

private const val VERIFICATION_BUNDLE = "verificationBundle"

internal class PhoneVerificationFragmentThemeOne : BaseFragment(), PhoneVerificationContract.View {

    override var delegate: PhoneVerificationContract.Delegate? = null
    @VisibleForTesting(otherwise = Modifier.PRIVATE) val viewModel: VerificationViewModel by viewModel()
    @VisibleForTesting(otherwise = Modifier.PRIVATE) lateinit var verification: Verification
    @VisibleForTesting(otherwise = Modifier.PRIVATE) lateinit var phoneNumber: String

    override fun layoutId() = R.layout.fragment_phone_verification_theme_one

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun setUpArguments() {
        verification = arguments!![VERIFICATION_BUNDLE] as Verification
        phoneNumber = verification.verificationDataPoint!!
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

    override fun viewLoaded() = viewModel.viewLoaded(DataPoint.Type.PHONE)

    private fun setupTexts() {
        tv_phone_number.text = phoneNumber
    }

    private fun applyFontsAndColors() {
        tb_llsdk_toolbar.setTitleTextColor(UIConfig.textTopBarPrimaryColor)
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeFormLabel(tv_verification_code_title)
            customizeFormField(tv_phone_number)
            customizeFormFieldSmall(tv_resend_label)
            customizeFormFieldSmall(tv_expired_pin_label)
            customizeFormFieldSmall(tv_resend_countdown_label)
            customizeFormTextLink(tv_resend_bttn)
        }
    }

    private fun setupToolBar() = delegate?.configureToolbar(
            toolbar = tb_llsdk_toolbar,
            title = "auth_verify_phone_title".localized(),
            backButtonMode = BaseActivity.BackButtonMode.Close(null, UIConfig.iconTertiaryColor)
    )

    override fun setupListeners() {
        super.setupListeners()
        apto_pin_view.setOnCompleteListener { completed, _ ->
            if (completed) finishVerification()
        }
        tv_resend_bttn.setOnClickListener { resendVerification() }
    }

    private fun resendVerification() {
        showLoading()
        hideKeyboard()
        viewModel.restartVerification {
            hideLoading()
            notify("auth.verify_phone.resent.message".localized(), MessageBanner.MessageType.HEADS_UP)
            showKeyboard()
        }
    }

    private fun finishVerification() {
        showLoading()
        hideKeyboard()
        viewModel.finishVerification(apto_pin_view.pinResults) { result ->
            hideLoading()
            result.either(::handleFailure) { verification ->
                when (verification.status) {
                    VerificationStatus.PASSED -> {
                        try {
                            val dataPoint = PhoneDataPoint(
                                    verification = verification,
                                    phoneNumber = StringUtils.parsePhoneNumber(phoneNumber)
                            )
                            delegate?.onPhoneVerificationPassed(dataPoint)
                        }
                        catch(exception: NumberParseException) {
                            notify(exception.localizedMessage)
                        }
                    }
                    VerificationStatus.FAILED, VerificationStatus.PENDING -> {
                        notify(
                            "auth.verify_phone.error_wrong_code.title".localized(),
                            "auth.verify_phone.error_wrong_code.message".localized()
                        )
                        apto_pin_view.clear()
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
                    tv_resend_bttn.show()
                    tv_resend_countdown_label.hide()
                }
                is ResendButtonState.Waiting -> {
                    val waitTimeDescription = state.pendingSeconds.stringFromTimeInterval()
                    val newText = "auth.verify_phone.resent_wait_text".localized()
                            .replace("<<WAIT_TIME>>", waitTimeDescription)
                    tv_resend_countdown_label.text = newText
                    tv_resend_bttn.hide()
                    tv_resend_countdown_label.show()
                }
            }
        }
    }

    override fun onBackPressed() {
        hideKeyboard()
        delegate?.onBackFromPhoneVerification()
    }

    companion object {
        fun newInstance(verification: Verification) = PhoneVerificationFragmentThemeOne().apply {
            this.arguments = Bundle().apply { putSerializable(VERIFICATION_BUNDLE, verification) }
        }
    }
}
