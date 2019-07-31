package com.aptopayments.sdk.features.auth.inputemail

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.TextView
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.user.Verification
import com.aptopayments.core.data.user.VerificationStatus
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.failure
import com.aptopayments.sdk.core.extension.observe
import com.aptopayments.sdk.core.extension.viewModel
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.core.ui.State
import com.aptopayments.sdk.utils.StringUtils
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_email_input_theme_one.*
import kotlinx.android.synthetic.main.include_toolbar_two.*

@VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
internal class InputEmailFragmentThemeOne : BaseFragment(), InputEmailContract.View {

    override var delegate: InputEmailContract.Delegate? = null
    private lateinit var viewModel: InputEmailViewModel
    private var menu: Menu? = null

    override fun layoutId() = R.layout.fragment_email_input_theme_one

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appComponent.inject(this)
    }

    override fun onPresented() {
        delegate?.configureStatusBar()
        et_email.requestFocus()
        showKeyboard()
    }

    @SuppressLint("SetTextI18n")
    override fun setupUI() {
        setupToolBar()
        applyFontsAndColors()
        context?.let {
            et_email.hint = "auth_input_email_hint".localized(it)
            tv_email_label.text = "auth_input_email_explanation".localized(it)
        }
    }

    override fun viewLoaded() {
        viewModel.viewLoaded()
    }

    private fun applyFontsAndColors() {
        view?.setBackgroundColor(UIConfig.uiBackgroundPrimaryColor)
        tb_llsdk_toolbar.setTitleTextColor(UIConfig.textTopBarColor)
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeFormLabel(tv_email_label)
            customizeFormField(et_email)
        }
    }

    private fun setupToolBar() {
        delegate?.configureToolbar(
                toolbar = tb_llsdk_toolbar,
                title = context?.let { "auth_input_email_title".localized(it) },
                backButtonMode = BaseActivity.BackButtonMode.Back(null, UIConfig.iconTertiaryColor)
        )
        context?.let { styleMenuItem(it) }
    }

    override fun setupListeners() {
        super.setupListeners()
        et_email.addTextChangedListener(object: TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                updateNextButtonState(StringUtils.isValidEmail(s.toString()))
            }
        })
    }

    private fun updateNextButtonState(nextButtonEnabled: Boolean?) {
        nextButtonEnabled?.let {
            val tvNext = tb_llsdk_toolbar.findViewById<TextView>(R.id.tv_menu_next)
            menu?.findItem(R.id.menu_toolbar_next_button)?.isEnabled = it
            if (it) { tvNext.setTextColor(UIConfig.textTopBarColor) } else { tvNext.setTextColor(UIConfig.disabledTextTopBarColor) }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun styleMenuItem(context: Context) {
        tb_llsdk_toolbar.post {
            val tvNext = tb_llsdk_toolbar.findViewById<TextView>(R.id.tv_menu_next)
            themeManager().customizeMenuItem(tvNext)
            tvNext.text = "toolbar_next_button_label".localized(context)
            tvNext.setTextColor(UIConfig.disabledTextTopBarColor)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_toolbar_next_button, menu)
        setupMenuItem(menu, R.id.menu_toolbar_next_button)
        if (this.menu == null) this.menu = menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_toolbar_next_button) {
            handleNextButtonClick()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun setupViewModel() {
        viewModel = viewModel(viewModelFactory) {
            observe(enableNextButton, ::updateNextButtonState)
            observe(state, ::updateProgressState)
            observe(verificationData, ::updateVerificationState)
            failure(failure) {
                hideLoading()
                handleFailure(it)
            }
        }
    }

    private fun updateVerificationState(verification: Verification?) {
        verification?.verificationDataPoint = et_email.text.toString()
        verification?.let {
            hideLoading()
            if (it.status == VerificationStatus.PENDING) {
                hideKeyboard()
                delegate?.onEmailVerificationStarted(verification)
            }
        }
    }

    private fun updateProgressState(state: State?) {
        val isInProgress = state == State.IN_PROGRESS
        if (isInProgress) showLoading() else hideLoading()
    }

    private fun handleNextButtonClick() {
        val emailText = et_email.text.toString()
        showLoading()
        hideKeyboard()
        viewModel.startVerificationUseCase(emailText)
    }

    override fun onBackPressed() {
        hideKeyboard()
        delegate?.onBackFromInputEmail()
    }

    companion object {
        fun newInstance() = InputEmailFragmentThemeOne()
    }
}
