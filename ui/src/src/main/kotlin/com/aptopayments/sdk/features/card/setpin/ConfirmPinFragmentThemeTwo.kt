package com.aptopayments.sdk.features.card.setpin

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.failure
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.TextInputWatcher
import com.aptopayments.sdk.utils.ValidInputListener
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_set_pin_theme_two.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.reflect.Modifier

private const val PIN_CHARACTERS = 4
private const val PIN_KEY = "PIN"

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class ConfirmPinFragmentThemeTwo : BaseFragment(), ConfirmPinContract.View {

    private val viewModel: ConfirmPinViewModel by viewModel()
    private lateinit var pin: String
    override var delegate: ConfirmPinContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_set_pin_theme_two

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun setUpArguments() {
        pin = arguments!![PIN_KEY] as String
    }

    override fun setupViewModel() {
        viewModel.apply {
            failure(failure) { handleFailure(it) }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onPresented() {
        delegate?.configureStatusBar()
        pin_view.text?.clear()
        pin_view.requestFocus()
        showKeyboard()
    }

    override fun setupUI() {
        setupTexts()
        setupTheme()
        setupToolBar()
    }

    @SuppressLint("SetTextI18n")
    private fun setupTexts() {
        tv_set_pin_title.text = "manage_card.confirm_pin.title".localized()
        tv_set_pin_explanation.text = "manage_card.confirm_pin.explanation".localized()
    }

    private fun setupTheme() {
        with (themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_set_pin_title)
            customizeRegularTextLabel(tv_set_pin_explanation)
        }
    }

    private fun setupToolBar() {
        delegate?.configureToolbar(
                toolbar = tb_llsdk_toolbar,
                title = null,
                backButtonMode = BaseActivity.BackButtonMode.Back(null)
        )
    }

    override fun setupListeners() {
        super.setupListeners()
        val validator = object : ValidInputListener {
            override fun onValidInput(isValid: Boolean) {
                if (isValid) {
                    val inputPin = pin_view.text.toString()
                    if (inputPin == pin) handleConfirmedPin(inputPin)
                    else handleWrongPin()
                }
            }
        }
        pin_view.addTextChangedListener(TextInputWatcher(validator, PIN_CHARACTERS, pin_view))
    }

    private fun handleConfirmedPin(pin: String) {
        hideKeyboard()
        delegate?.pinConfirmed(pin)
    }

    private fun handleWrongPin() {
        onBackPressed()
        notify(
            "manage_card.confirm_pin.error_wrong_code.title".localized(),
            "manage_card.confirm_pin.error_wrong_code.message".localized()
        )
    }

    override fun onBackPressed() {
        delegate?.onBackFromPinConfirmation()
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    companion object {
        fun newInstance(pin: String) = ConfirmPinFragmentThemeTwo().apply {
            arguments = Bundle().apply { putString(PIN_KEY, pin) }
        }
    }
}
