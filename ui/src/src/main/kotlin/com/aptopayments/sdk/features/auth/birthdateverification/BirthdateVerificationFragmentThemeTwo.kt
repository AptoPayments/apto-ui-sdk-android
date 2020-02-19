package com.aptopayments.sdk.features.auth.birthdateverification

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.user.DataPoint
import com.aptopayments.core.data.user.Verification
import com.aptopayments.core.data.user.VerificationStatus
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.failure
import com.aptopayments.sdk.core.extension.observeNotNullable
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

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class BirthdateVerificationFragmentThemeTwo : BaseFragment(), BirthdateVerificationContract.View {

    override var delegate: BirthdateVerificationContract.Delegate? = null
    private val viewModel: BirthdateVerificationViewModel by viewModel()
    private lateinit var primaryCredential: DataPoint
    private lateinit var editTextList: MutableList<EditText>
    private lateinit var birthdayDay: EditText
    private lateinit var birthdayMonth: EditText
    private lateinit var birthdayYear: EditText

    override fun layoutId() = R.layout.fragment_birthdate_verification_theme_two

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun setUpArguments() {
        primaryCredential = arguments!![BIRTHDATE_VERIFICATION_BUNDLE] as DataPoint
    }

    override fun onPresented() {
        delegate?.configureStatusBar()
    }

    override fun setupViewModel() {
        viewModel.apply {
            observeNotNullable(birthdateVerification, ::handleBirthdateVerification)
            observeNotNullable(continueEnabled, ::updateButtonState)
            observeNotNullable(dateOrder) { configureViewOrder(it) }
            failure(failure) {
                hideLoading()
                handleFailure(it)
            }
        }
    }

    override fun setupUI() {
        continue_button.isEnabled = false
        setupTheme()
        setupToolbar()
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    override fun setupListeners() {
        continue_button.setOnClickListener { onContinueButtonPressed() }
    }

    private fun configureDateComponent(view: EditText, component: DateComponent, onChange: (String) -> Unit) {
        val listener = object : ValidInputListener {
            override fun onValidInput(isValid: Boolean) {
                setColorForInput(view, isValid)
                if (isValid) {
                    focusNext(view)
                }
                onChange(if (isValid) view.text.toString() else "")
            }
        }
        view.addTextChangedListener(TextInputWatcher(listener, component.length, view, component.regex))
    }

    private fun focusNext(editText: EditText) {
        val current = editTextList.indexOf(editText)
        editTextList.getOrNull(current + 1)?.requestFocus()
    }

    override fun onBackPressed() {
        delegate?.onBackFromBirthdateVerification()
    }

    private fun setupTheme() {
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_birthdate_title)
            customizeFormLabel(tv_verification_code_header)
            customizeSubmitButton(continue_button)
        }
    }

    private fun setupToolbar()  {
        tb_llsdk_toolbar.setBackgroundColor(UIConfig.uiNavigationPrimaryColor)
        delegate?.configureToolbar(
            toolbar = tb_llsdk_toolbar,
            title = null,
            backButtonMode = BaseActivity.BackButtonMode.Back(null)
        )
    }

    private fun onContinueButtonPressed() {
        primaryCredential.verification?.secondaryCredential?.verificationId?.let {
            showLoading()
            hideKeyboard()
            viewModel.onContinueButtonPressed(it)
        }
    }

    private fun handleBirthdateVerification(verification: Verification) {
        hideLoading()
        if (verification.status == VerificationStatus.PASSED) {
            delegate?.onBirthdateVerificationPassed(primaryCredential.verification!!, verification)
        } else {
            notifyVerificationError()
            clearAllEditTexts()
        }
    }

    private fun notifyVerificationError() {
        notify(
            "auth.verify_birthdate.error_wrong_code.title".localized(),
            "auth.verify_birthdate.error_wrong_code.message".localized()
        )
    }

    private fun clearAllEditTexts() = editTextList.forEach {
        it.text.clear()
    }

    private fun updateButtonState(enabled: Boolean) {
        continue_button.isEnabled = enabled
    }

    private fun configureViewOrder(dateOrder: DateFormatOrder) {
        showCorrectStub(dateOrder)
        bindDateViews()
        setEditTextOrderList(dateOrder)
        configureDateViewsListeners()
        focusFirst()
    }

    private fun showCorrectStub(dateOrderDate: DateFormatOrder) {
        when (dateOrderDate) {
            DateFormatOrder.MDY -> stub_mdy
            DateFormatOrder.YMD -> stub_ymd
            else -> stub_dmy
        }.inflate()
    }

    private fun bindDateViews() {
        birthdayDay = container.findViewById(R.id.et_birthday_day)
        birthdayMonth = container.findViewById(R.id.et_birthday_month)
        birthdayYear = container.findViewById(R.id.et_birthday_year)
        container.findViewById<View>(R.id.separator_left).setBackgroundColor(UIConfig.uiBackgroundPrimaryColor)
        container.findViewById<View>(R.id.separator_right).setBackgroundColor(UIConfig.uiBackgroundPrimaryColor)
        with(themeManager()){
            customizeEditText(birthdayDay)
            customizeEditText(birthdayMonth)
            customizeEditText(birthdayYear)
        }
    }

    private fun setEditTextOrderList(isDayMonth: DateFormatOrder) {
        editTextList = when (isDayMonth) {
            DateFormatOrder.MDY -> mutableListOf(birthdayMonth, birthdayDay, birthdayYear)
            DateFormatOrder.YMD -> mutableListOf(birthdayYear, birthdayMonth, birthdayDay)
            else -> mutableListOf(birthdayDay, birthdayMonth, birthdayYear)
        }
    }

    private fun configureDateViewsListeners() {
        configureDateComponent(birthdayDay, DateComponent.DAY) { viewModel.setDay(it) }
        configureDateComponent(birthdayMonth, DateComponent.MONTH) { viewModel.setMonth(it) }
        configureDateComponent(birthdayYear, DateComponent.YEAR) { viewModel.setYear(it) }
    }

    private fun focusFirst() {
        editTextList.firstOrNull()?.requestFocus()
        showKeyboard()
    }

    private fun setColorForInput(field: EditText, isPassed: Boolean) =
        field.setTextColor(if (isPassed) UIConfig.textPrimaryColor else UIConfig.uiErrorColor)

    companion object {
        fun newInstance(primaryCredential: DataPoint) = BirthdateVerificationFragmentThemeTwo().apply {
            this.arguments = Bundle().apply { putSerializable(BIRTHDATE_VERIFICATION_BUNDLE, primaryCredential) }
        }
    }
}
