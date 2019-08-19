package com.aptopayments.sdk.features.auth.verification

import android.annotation.SuppressLint
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
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.MessageBanner
import com.aptopayments.sdk.utils.StringUtils
import com.aptopayments.sdk.utils.TextInputWatcher
import com.aptopayments.sdk.utils.ValidInputListener
import com.google.android.material.appbar.AppBarLayout
import com.google.i18n.phonenumbers.NumberParseException
import kotlinx.android.synthetic.main.fragment_verification_theme_two.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.reflect.Modifier

private const val VERIFICATION_BUNDLE = "verificationBundle"
private const val PIN_CHARACTERS = 6

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class PhoneVerificationFragmentThemeTwo : BaseFragment(), PhoneVerificationContract.View {

    override var delegate: PhoneVerificationContract.Delegate? = null
    @VisibleForTesting(otherwise = Modifier.PRIVATE) val viewModel: VerificationViewModel by viewModel()
    @VisibleForTesting(otherwise = Modifier.PRIVATE) lateinit var verification: Verification
    @VisibleForTesting(otherwise = Modifier.PRIVATE) lateinit var phoneNumber: String

    override fun layoutId() = R.layout.fragment_verification_theme_two

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            failure(failure) { handleFailure(it) } }
        viewModel.verification.postValue(verification)
    }

    override fun setupUI() {
        applyFontsAndColors()
        setupToolBar()
    }

    override fun viewLoaded() = viewModel.viewLoaded(DataPoint.Type.PHONE)

    @SuppressLint("SetTextI18n")
    private fun applyFontsAndColors() {
        context?.let {
            tv_verification_code_title.text = "auth.verify_phone.title".localized(it)
            tv_verification_code_header.text = "auth.verify_phone.explanation".localized(it)
            tv_resend_label.text = "auth.verify_phone.footer".localized(it)
            tv_resend_bttn.text = "auth.verify_phone.resend_button.title".localized(it)
            tv_expired_pin_label.text = "auth.verify_phone.expired_pin.text".localized(it)
        }
        view!!.setBackgroundColor(UIConfig.uiNavigationPrimaryColor)
        with(themeManager()){
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_verification_code_title)
            customizeFormLabel(tv_verification_code_header)
            customizeFormTextLink(tv_resend_bttn)
            customizeFooterLabel(tv_resend_label)
            customizeFooterLabel(tv_resend_countdown_label)
            customizeErrorLabel(tv_expired_pin_label)
        }
    }

    private fun setupToolBar() = delegate?.configureToolbar(
            toolbar = tb_llsdk_toolbar,
            title = null,
            backButtonMode = BaseActivity.BackButtonMode.Back(null)
    )

    override fun setupListeners() {
        super.setupListeners()
        val validator = object : ValidInputListener {
            override fun onValidInput(isValid: Boolean) {
                if (isValid) finishVerification()
            }
        }
        apto_pin_view.addTextChangedListener(TextInputWatcher(validator, PIN_CHARACTERS, apto_pin_view))
        tv_resend_bttn.setOnClickListener { resendVerification() }
    }

    private fun resendVerification() {
        showLoading()
        hideKeyboard()
        viewModel.restartVerification {
            hideLoading()
            context?.let { context ->
                notify("auth.verify_phone.resent.message".localized(context), MessageBanner.MessageType.HEADS_UP)
            }
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
                                    phoneNumber = StringUtils.parsePhoneNumber(phoneNumber)
                            )
                            delegate?.onPhoneVerificationPassed(dataPoint)
                        }
                        catch(exception: NumberParseException) {
                            notify(exception.localizedMessage)
                        }
                    }
                    VerificationStatus.FAILED, VerificationStatus.PENDING -> {
                        context?.let {
                            notify("auth.verify_phone.error_wrong_code.title".localized(it), "auth.verify_phone.error_wrong_code.message".localized(it))
                            apto_pin_view.text?.clear()
                        }
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
                    context?.let {
                        val waitTimeDescription = state.pendingSeconds.stringFromTimeInterval()
                        val newText = "auth.verify_phone.resent_wait_text".localized(it).replace("<<WAIT_TIME>>", waitTimeDescription)
                        tv_resend_countdown_label.text = newText
                    }
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
        fun newInstance(verification: Verification) = PhoneVerificationFragmentThemeTwo().apply {
            this.arguments = Bundle().apply { putSerializable(VERIFICATION_BUNDLE, verification) }
        }
    }
}
