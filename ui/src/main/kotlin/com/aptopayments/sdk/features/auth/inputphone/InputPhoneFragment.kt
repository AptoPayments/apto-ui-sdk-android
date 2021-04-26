package com.aptopayments.sdk.features.auth.inputphone

import android.os.Bundle
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.geo.Country
import com.aptopayments.mobile.data.user.Verification
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.*
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.ui.views.PhoneInputView
import com.aptopayments.sdk.utils.extensions.setOnClickListenerSafe
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_phone_input.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.Serializable

private const val ALLOWED_COUNTRIES_KEY = "ALLOWED_COUNTRIES"

internal class InputPhoneFragment : BaseFragment(), InputPhoneContract.View {

    override var delegate: InputPhoneContract.Delegate? = null
    private val viewModel: InputPhoneViewModel by viewModel()
    private lateinit var allowedCountriesList: List<Country>

    override fun layoutId() = R.layout.fragment_phone_input

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    @Suppress("UNCHECKED_CAST")
    override fun setUpArguments() {
        allowedCountriesList = requireArguments()[ALLOWED_COUNTRIES_KEY] as List<Country>
    }

    override fun onPresented() {
        customizePrimaryNavigationStatusBar()
        phone_input?.requestPhoneFocus()
        showKeyboard()
    }

    override fun setupUI() {
        applyFontsAndColors()
        setupViews()
        setupToolBar()
    }

    private fun setupViews() {
        phone_input.delegate = object : PhoneInputView.Delegate {
            override fun onPhoneInputChanged(phoneNumber: String, valid: Boolean) {
                viewModel.onPhoneChanged(phoneNumber, valid)
            }

            override fun onCountryChanged(countryCode: String) {
                viewModel.onCountryChanged(countryCode)
            }
        }
        phone_input.setAllowedCountriesIsoCode(allowedCountriesList.map { it.isoCode })
    }

    private fun applyFontsAndColors() {
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_phone_header)
            customizeFormLabel(tv_phone_label)
            customizeSubmitButton(continue_button)
            customizeRoundedBackground(phone_input)
        }
    }

    private fun setupToolBar() {
        tb_llsdk_toolbar.configure(
            this,
            ToolbarConfiguration.Builder()
                .backButtonMode(getBackButtonMode())
                .backgroundColor(UIConfig.uiNavigationPrimaryColor)
                .build()
        )
    }

    private fun getBackButtonMode(): BackButtonMode {
        return if (viewModel.showXOnToolbar) {
            BackButtonMode.Close()
        } else {
            BackButtonMode.Back()
        }
    }

    override fun setupListeners() {
        super.setupListeners()
        continue_button.setOnClickListenerSafe { onContinueButtonClicked() }
    }

    override fun setupViewModel() {
        viewModel.apply {
            observeNotNullable(enableNextButton) { continue_button.isEnabled = it }
            observeNotNullable(viewModel.loading) { handleLoading(it) }
            observeNullable(verificationData, ::updateVerificationState)
            observeNullable(failure) { handleFailure(it) }
        }
    }

    private fun updateVerificationState(verification: Verification?) {
        verification?.let {
            delegate?.onPhoneVerificationStarted(verification)
        }
    }

    private fun onContinueButtonClicked() {
        hideKeyboard()
        viewModel.onContinueClicked()
    }

    override fun onBackPressed() {
        hideKeyboard()
        delegate?.onBackFromInputPhone()
    }

    companion object {
        fun newInstance(allowedCountriesList: List<Country>) = InputPhoneFragment().apply {
            val allowedCountries = listOrDefault(allowedCountriesList)
            this.arguments = Bundle().apply { putSerializable(ALLOWED_COUNTRIES_KEY, allowedCountries as Serializable) }
        }

        private fun listOrDefault(allowedCountriesList: List<Country>) =
            allowedCountriesList.ifEmpty { listOf(Country("US")) }
    }
}
