package com.aptopayments.sdk.features.auth.birthdateverification

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.widget.EditText
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.user.DataPoint
import com.aptopayments.core.data.user.Verification
import com.aptopayments.core.data.user.VerificationStatus
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.failure
import com.aptopayments.sdk.core.extension.observe
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.TextInputWatcher
import com.aptopayments.sdk.utils.ValidInputListener
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_birthdate_verification_theme_two.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.reflect.Modifier

const val BIRTHDATE_VERIFICATION_BUNDLE = "birthdateVerificationBundle"
private const val DAY_OF_MONTH_REGEX = "(0?[1-9]|[12][0-9]|3[01])"
private const val MONTH_OF_YEAR_REGEX = "(0?[1-9]|1[012])"
private const val YEAR_REGEX = "^(19|20)\\d{2}$"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class BirthdateVerificationFragmentThemeTwo: BaseFragment(), BirthdateVerificationContract.View {

    override fun layoutId() = R.layout.fragment_birthdate_verification_theme_two
    private val viewModel: BirthdateVerificationViewModel by viewModel()
    private lateinit var primaryCredential: DataPoint
    override var delegate: BirthdateVerificationContract.Delegate? = null

    override fun setUpArguments() {
        primaryCredential = arguments!![BIRTHDATE_VERIFICATION_BUNDLE] as DataPoint
    }

    override fun onPresented() {
        delegate?.configureStatusBar()
        et_birthday_day.requestFocus()
        showKeyboard()
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(birthdateVerification, ::handleBirthdateVerification)
            observe(enableNextButton, ::updateButtonState)
            failure(failure) {
                hideLoading()
                handleFailure(it)
            }
        }
    }

    override fun setupUI() {
        continue_button.isEnabled = false
        setupTheme()
        setupTexts()
        setupToolbar()
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    override fun setupListeners() {
        super.setupListeners()
        continue_button.setOnClickListener { handleButtonClick() }
        val dayValidator = object : ValidInputListener {
            override fun onValidInput(isValid: Boolean) {
                updateForValidatingUserInput(et_birthday_day, isValid)
                if (isValid) focusHandler(et_birthday_day, et_birthday_month)
            }
        }
        et_birthday_day.addTextChangedListener(TextInputWatcher(dayValidator, 2, et_birthday_month, DAY_OF_MONTH_REGEX))
        val monthValidator = object : ValidInputListener {
            override fun onValidInput(isValid: Boolean) {
                updateForValidatingUserInput(et_birthday_month, isValid)
                if (isValid) focusHandler(et_birthday_month, et_birthday_year)
            }
        }
        et_birthday_month.addTextChangedListener(TextInputWatcher(monthValidator, 2, et_birthday_month, MONTH_OF_YEAR_REGEX))
        val yearValidator = object : ValidInputListener {
            override fun onValidInput(isValid: Boolean) {
                updateButtonState(isValid)
                updateForValidatingUserInput(et_birthday_year, isValid)
            }
        }
        et_birthday_year.addTextChangedListener(TextInputWatcher(yearValidator, 4, et_birthday_year, YEAR_REGEX))
    }

    override fun onBackPressed() {
        delegate?.onBackFromBirthdateVerification()
    }

    @SuppressLint("SetTextI18n")
    private fun setupTexts() = context?.let {
        tv_birthdate_title.text = "auth.verify_birthdate.title".localized(it)
        tv_verification_code_header.text = "auth.verify_birthdate.explanation".localized(it)
        continue_button.text = "auth.verify_birthdate.call_to_action.title".localized(it)
    }

    private fun setupTheme() {
        view?.setBackgroundColor(UIConfig.uiBackgroundPrimaryColor)
        with (themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_birthdate_title)
            customizeFormLabel(tv_verification_code_header)
            customizeSubmitButton(continue_button)
        }
    }

    private fun setupToolbar() = delegate?.configureToolbar(
            toolbar = tb_llsdk_toolbar,
            title = null,
            backButtonMode = BaseActivity.BackButtonMode.Back(null)
    )

    private fun handleButtonClick() {
        val day = et_birthday_day.text.toString()
        val month = et_birthday_month.text.toString()
        val year = et_birthday_year.text.toString()
        primaryCredential.verification?.secondaryCredential?.verificationId?.let {
            showLoading()
            hideKeyboard()
            viewModel.finishVerification(it, day, month, year)
        }
    }

    private fun handleBirthdateVerification(verification: Verification?) {
        if (verification?.status == VerificationStatus.PASSED) {
            delegate?.onBirthdateVerificationPassed(primaryCredential.verification!!, verification)
        } else {
            hideLoading()
            context?.let {
                notify("auth.verify_birthdate.error_wrong_code.title".localized(it),
                        "auth.verify_birthdate.error_wrong_code.message".localized(it))
            }
            et_birthday_day.text.clear()
            et_birthday_month.text.clear()
            et_birthday_year.text.clear()
        }
    }

    private fun updateButtonState(continueButtonEnabled: Boolean?) {
        continueButtonEnabled?.let {
            continue_button.isEnabled = continueButtonEnabled
        }
    }

    private fun updateForValidatingUserInput(field: EditText, isPassed: Boolean) =
            field.setTextColor(if (isPassed) UIConfig.textPrimaryColor else UIConfig.uiErrorColor)

    private fun focusHandler(lastView: EditText, nextView: EditText) {
        val handler = Handler()
        handler.postDelayed({
            lastView.clearFocus()
            nextView.requestFocus()
        }, 2)
    }

    companion object {
        fun newInstance(primaryCredential: DataPoint) = BirthdateVerificationFragmentThemeTwo().apply {
            this.arguments = Bundle().apply { putSerializable(BIRTHDATE_VERIFICATION_BUNDLE, primaryCredential) }
        }
    }
}
