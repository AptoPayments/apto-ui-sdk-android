package com.aptopayments.sdk.features.passcode

import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.extension.visibleIf
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.features.card.CardActivity
import com.aptopayments.sdk.utils.TextInputWatcher
import com.aptopayments.sdk.utils.ValidInputListener
import com.aptopayments.sdk.utils.shake
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_passcode.*
import kotlinx.android.synthetic.main.include_toolbar_two.*

private const val PASSCODE_CHARACTERS = 4

internal abstract class PasscodeFragment : BaseFragment(), PasscodeContract.View {

    override var delegate: PasscodeContract.Delegate? = null
    override fun layoutId(): Int = R.layout.fragment_passcode
    protected abstract val viewModel: PasscodeViewModel

    override fun setupViewModel() {
        observeNotNullable(viewModel.title) { tv_set_pin_title.localizedText = it }
        observeNotNullable(viewModel.subtitle) { tv_set_pin_subtitle.localizedText = it }
        observeNotNullable(viewModel.wrongPin) {
            if (it) {
                wrongPin()
            }
        }
        observeNotNullable(viewModel.backpressed) {
            delegate?.onBackPressed()
            hideKeyboard()
        }
        observeNotNullable(viewModel.correctPin) {
            delegate?.onPasscodeSetCorrectly(it)
            hideKeyboard()
        }
        observeNotNullable(viewModel.clearView) {
            pin_view.text?.clear()
        }
        observeNotNullable(viewModel.showForgot) { tv_forgot_passcode.visibleIf(it) }
    }

    override fun viewLoaded() {
        super.viewLoaded()
        viewModel.viewLoaded()
    }

    private fun wrongPin() {
        pin_view.shake()
        pin_view.text?.clear()
        notify(
            "biometric_create_pin_error_title".localized(),
            "biometric.create_pin_error_pin_not_match".localized()
        )
    }

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun onPresented() {
        customizePrimaryNavigationStatusBar()
        pin_view.requestFocus()
        showKeyboard()
    }

    override fun setupUI() {
        with(themeManager()) {
            pin_view.setTextColor(UIConfig.textPrimaryColor)
            customizeRoundedBackground(pin_view)
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_set_pin_title)
            customizeRegularTextLabel(tv_set_pin_subtitle)
            customizeFormTextLink(tv_forgot_passcode)
        }
        setupToolBar()
    }

    override fun setupListeners() {
        super.setupListeners()
        val validator = object : ValidInputListener {
            override fun onValidInput(isValid: Boolean) {
                if (isValid) onPinEntered(pin_view.text.toString())
            }
        }
        pin_view.addTextChangedListener(TextInputWatcher(validator, PASSCODE_CHARACTERS, pin_view))
        tv_forgot_passcode.setOnClickListener { onForgotPressed() }
    }

    private fun setupToolBar() {
        tb_llsdk_toolbar.configure(activity, ToolbarConfiguration.Builder().setPrimaryColors().build())
    }

    fun onPinEntered(currentPin: String) {
        viewModel.onPasscodeInserted(currentPin)
    }

    private fun onForgotPressed() {
        (activity as CardActivity).onAuthForgot()
    }

    override fun onBackPressed() {
        viewModel.onBackPressed()
    }
}
