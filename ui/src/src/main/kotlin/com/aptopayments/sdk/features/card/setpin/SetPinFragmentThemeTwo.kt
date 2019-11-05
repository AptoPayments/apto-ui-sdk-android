package com.aptopayments.sdk.features.card.setpin

import android.annotation.SuppressLint
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

@VisibleForTesting(otherwise = Modifier.PROTECTED)
internal class SetPinFragmentThemeTwo : BaseFragment(), SetPinContract.View {

    private val viewModel: SetPinViewModel by viewModel()
    override var delegate: SetPinContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_set_pin_theme_two

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
        tv_set_pin_title.text = "manage_card.set_pin.title".localized()
        tv_set_pin_explanation.text = "manage_card.set_pin.explanation".localized()
    }

    private fun setupTheme() {
        view?.setBackgroundColor(UIConfig.uiBackgroundPrimaryColor)
        with (themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_set_pin_title)
            customizeRegularTextLabel(tv_set_pin_explanation)
        }
    }

    private fun setupToolBar() = delegate?.configureToolbar(
            toolbar = tb_llsdk_toolbar,
            title = null,
            backButtonMode = BaseActivity.BackButtonMode.Close(null))

    override fun setupListeners() {
        super.setupListeners()
        val validator = object : ValidInputListener {
            override fun onValidInput(isValid: Boolean) {
                if (isValid) delegate?.setPinFinished(pin_view.text.toString())
            }
        }
        pin_view.addTextChangedListener(TextInputWatcher(validator, PIN_CHARACTERS, pin_view))
    }



    override fun onBackPressed() {
        hideKeyboard()
        delegate?.onCloseFromSetPin()
    }

    override fun viewLoaded() = viewModel.viewLoaded()
}
