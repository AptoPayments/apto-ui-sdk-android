package com.aptopayments.sdk.features.auth.birthdateverification

import android.os.Bundle
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.ui.views.birthdate.BirthdateView
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_birthdate_verification.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import org.threeten.bp.LocalDate

const val BIRTHDATE_VERIFICATION_BUNDLE = "birthdateVerificationBundle"

internal class BirthdateVerificationFragment : BaseFragment(), BirthdateVerificationContract.View {

    override var delegate: BirthdateVerificationContract.Delegate? = null
    private val viewModel: BirthdateVerificationViewModel by viewModel { parametersOf(verification) }
    private lateinit var verification: Verification

    override fun layoutId() = R.layout.fragment_birthdate_verification

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun setUpArguments() {
        verification = requireArguments()[BIRTHDATE_VERIFICATION_BUNDLE] as Verification
    }

    override fun onPresented() {
        customizePrimaryNavigationStatusBar()
    }

    override fun setupViewModel() {
        viewModel.apply {
            observeNotNullable(birthdateVerified) { delegate?.onBirthdateVerificationPassed(it) }
            observeNotNullable(verificationError) { notifyVerificationError() }
            observeNotNullable(viewModel.loading) { handleLoading(it) }
            observeNotNullable(continueEnabled, ::updateButtonState)
        }
    }

    override fun setupUI() {
        setupTheme()
        setupToolbar()
        birthdate_view.delegate = object : BirthdateView.Delegate {
            override fun onDateInput(value: LocalDate?) {
                viewModel.setLocalDate(value)
            }
        }
    }

    override fun setupListeners() {
        continue_button.setOnClickListenerSafe {
            hideKeyboard()
            viewModel.onContinueClicked()
        }
    }

    override fun onBackPressed() {
        delegate?.onBackFromBirthdateVerification()
    }

    private fun setupTheme() {
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_birthdate_title)
            customizeFormLabel(tv_birthdate_subtitle)
            customizeSubmitButton(continue_button)
        }
    }

    private fun setupToolbar() {
        tb_llsdk_toolbar.configure(this, ToolbarConfiguration.Builder().setPrimaryColors().build())
    }

    private fun notifyVerificationError() {
        clearAllEditTexts()
        notify(
            "auth.verify_birthdate.error_wrong_code.title".localized(),
            "auth.verify_birthdate.error_wrong_code.message".localized()
        )
    }

    private fun clearAllEditTexts() = birthdate_view.clear()

    private fun updateButtonState(enabled: Boolean) {
        continue_button.isEnabled = enabled
    }

    companion object {
        fun newInstance(verification: Verification, tag: String) = BirthdateVerificationFragment().apply {
            this.arguments = Bundle().apply { putSerializable(BIRTHDATE_VERIFICATION_BUNDLE, verification) }
            TAG = tag
        }
    }
}
