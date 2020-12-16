package com.aptopayments.sdk.features.auth.verification

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.user.DataPoint
import com.aptopayments.mobile.data.user.PhoneDataPoint
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
import com.aptopayments.sdk.utils.extensions.parsePhoneNumber
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe
import com.aptopayments.sdk.utils.extensions.stringFromTimeInterval
import com.google.android.material.appbar.AppBarLayout
import com.google.i18n.phonenumbers.NumberParseException
import kotlinx.android.synthetic.main.fragment_phone_verification.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.reflect.Modifier

private const val VERIFICATION_BUNDLE = "verificationBundle"
private const val PIN_CHARACTERS = 6

internal class PhoneVerificationFragment : BaseFragment(), PhoneVerificationContract.View {

    override var delegate: PhoneVerificationContract.Delegate? = null
    private val viewModel: VerificationViewModel by viewModel()
    @VisibleForTesting(otherwise = Modifier.PRIVATE)
    lateinit var verification: Verification
    private lateinit var phoneNumber: String

    override fun layoutId() = R.layout.fragment_phone_verification

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun setUpArguments() {
        verification = requireArguments()[VERIFICATION_BUNDLE] as Verification
        phoneNumber = verification.verificationDataPoint!!
    }

    override fun onPresented() {
        customizePrimaryNavigationStatusBar()
        apto_pin_view?.requestFocus()
        showKeyboard()
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(pinEntryState, ::handlePinEntryState)
            observe(resendButtonState, ::handleResendButtonState)
            observe(failure) { handleFailure(it) }
        }
        viewModel.verification.postValue(verification)
    }

    override fun setupUI() {
        applyFontsAndColors()
        setupToolBar()
    }

    override fun viewLoaded() = viewModel.viewLoaded(DataPoint.Type.PHONE)

    private fun applyFontsAndColors() {
        with(themeManager()) {
            apto_pin_view.setTextColor(UIConfig.textPrimaryColor)
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_verification_code_title)
            customizeFormLabel(tv_verification_code_header)
            customizeFormTextLink(tv_resend_bttn)
            customizeFooterLabel(tv_resend_label)
            customizeFooterLabel(tv_resend_countdown_label)
            customizeErrorLabel(tv_expired_pin_label)
        }
    }

    private fun setupToolBar() {
        tb_llsdk_toolbar.configure(
            this,
            ToolbarConfiguration.Builder().backgroundColor(UIConfig.uiNavigationPrimaryColor).build()
        )
    }

    override fun setupListeners() {
        super.setupListeners()
        val validator = object : ValidInputListener {
            override fun onValidInput(isValid: Boolean) {
                if (isValid) finishVerification()
            }
        }
        apto_pin_view.addTextChangedListener(TextInputWatcher(validator, PIN_CHARACTERS, apto_pin_view))
        tv_resend_bttn.setOnClickListenerSafe { resendVerification() }
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
        viewModel.finishVerification(apto_pin_view.text.toString()) { result ->
            hideLoading()
            result.either(::handleFailure) { verification ->
                when (verification.status) {
                    VerificationStatus.PASSED -> {
                        try {
                            val dataPoint = PhoneDataPoint(
                                verification = verification,
                                phoneNumber = phoneNumber.parsePhoneNumber()
                            )
                            delegate?.onPhoneVerificationPassed(dataPoint)
                        } catch (exception: NumberParseException) {
                            exception.localizedMessage?.let { notify(it) }
                        }
                    }
                    VerificationStatus.FAILED, VerificationStatus.PENDING -> {
                        notify(
                            "auth.verify_phone.error_wrong_code.title".localized(),
                            "auth.verify_phone.error_wrong_code.message".localized()
                        )
                        apto_pin_view.text?.clear()
                    }
                }
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
                    val newText =
                        "auth.verify_phone.resent_wait_text".localized().replace("<<WAIT_TIME>>", waitTimeDescription)
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
        fun newInstance(verification: Verification) = PhoneVerificationFragment().apply {
            this.arguments = Bundle().apply { putSerializable(VERIFICATION_BUNDLE, verification) }
        }
    }
}
