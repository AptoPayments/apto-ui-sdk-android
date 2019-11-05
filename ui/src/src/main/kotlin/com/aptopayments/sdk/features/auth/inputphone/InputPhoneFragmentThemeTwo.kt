package com.aptopayments.sdk.features.auth.inputphone

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import androidx.annotation.VisibleForTesting
import com.aptopayments.core.data.geo.Country
import com.aptopayments.core.data.user.Verification
import com.aptopayments.core.data.user.VerificationStatus
import com.aptopayments.sdk.R
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.sdk.core.extension.failure
import com.aptopayments.core.extension.localized
import com.aptopayments.sdk.core.extension.observe
import com.aptopayments.sdk.core.extension.viewModel
import com.aptopayments.sdk.core.platform.BaseActivity
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.core.ui.State
import com.aptopayments.sdk.utils.AptoPhoneNumberWatcher
import com.aptopayments.sdk.utils.ValidInputListener
import com.aptopayments.sdk.utils.disableCountryPicker
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_phone_input_theme_two.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.Serializable

private const val ALLOWED_COUNTRIES_KEY = "ALLOWED_COUNTRIES"

@VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
internal class InputPhoneFragmentThemeTwo : BaseFragment(), InputPhoneContract.View {

    override var delegate: InputPhoneContract.Delegate? = null
    private lateinit var validator: ValidInputListener
    private lateinit var aptoPhoneNumberWatcher: AptoPhoneNumberWatcher
    private val viewModel: InputPhoneViewModel by viewModel()
    private lateinit var allowedCountriesList: List<Country>

    override fun layoutId() = R.layout.fragment_phone_input_theme_two

    @Suppress("UNCHECKED_CAST")
    override fun setUpArguments() {
        allowedCountriesList = arguments!![ALLOWED_COUNTRIES_KEY] as List<Country>
    }

    override fun onPresented() {
        delegate?.configureStatusBar()
        et_phone?.requestFocus()
        showKeyboard()
    }

    override fun setupUI() {
        applyFontsAndColors()
        configureInputPhoneView()
        setupToolBar()
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    @SuppressLint("SetTextI18n")
    private fun applyFontsAndColors() {
        tv_phone_header.text = "auth_input_phone_title".localized()
        tv_phone_label.text = "auth_input_phone_explanation".localized()
        continue_button.text = "auth_input_phone.call_to_action".localized()
        et_phone.hint = "auth_input_phone_hint".localized()

        view?.setBackgroundColor(UIConfig.uiNavigationPrimaryColor)

        with(themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_phone_header)
            customizeFormLabel(tv_phone_label)
            customizeSubmitButton(continue_button)
        }
    }

    private fun configureInputPhoneView() {
        val allowedCountriesArrayList = ArrayList<String>()
        for (country in allowedCountriesList.listIterator()) {
            allowedCountriesArrayList.add(country.isoCode)
        }
        country_code_picker.setDefaultCountryUsingNameCode(allowedCountriesArrayList[0])
        country_code_picker.resetToDefaultCountry()
        country_code_picker.setCustomMasterCountries(TextUtils.join(",", allowedCountriesArrayList))
        disableCountryPicker(allowedCountriesList.size == 1, country_code_picker)
    }

    private fun setupToolBar() = delegate?.configureToolbar(
            toolbar = tb_llsdk_toolbar,
            title = null,
            backButtonMode = BaseActivity.BackButtonMode.Back(null)
    )

    override fun setupListeners() {
        super.setupListeners()

        validator = object : ValidInputListener {
            override fun onValidInput(isValid: Boolean) {
                updateButtonState(isValid)
            }
        }
        aptoPhoneNumberWatcher = AptoPhoneNumberWatcher(allowedCountriesList[0].isoCode, validator)
        et_phone.addTextChangedListener(aptoPhoneNumberWatcher)
        country_code_picker.setOnCountryChangeListener {
            country_code_picker?.let {
                updatePhoneNumberWatcher(it.selectedCountryNameCode)
            }
        }
        continue_button.apply {
            isEnabled = false
            setOnClickListener { handleButtonClick() }
        }
    }

    private fun updateButtonState(continueButtonEnabled: Boolean?) {
        continueButtonEnabled?.let {
            continue_button.isEnabled = continueButtonEnabled
        }
    }

    override fun setupViewModel() {
        viewModel.apply {
            observe(enableNextButton, ::updateButtonState)
            observe(state, ::updateProgressState)
            observe(verificationData, ::updateVerificationState)
            failure(failure) { handleFailure(it) }
        }
    }

    private fun updatePhoneNumberWatcher(countryCode: String?) = countryCode?.let {
        aptoPhoneNumberWatcher.countryCode = countryCode
        et_phone.addTextChangedListener(aptoPhoneNumberWatcher)
    }

    private fun updateVerificationState(verification: Verification?) {
        verification?.let {
            hideLoading()
            if (it.status == VerificationStatus.PENDING) {
                hideKeyboard()
                delegate?.onPhoneVerificationStarted(verification)
            }
        }
    }

    private fun updateProgressState(state: State?) {
        val isInProgress = state == State.IN_PROGRESS
        if (isInProgress) showLoading() else hideLoading()
    }

    private fun handleButtonClick() {
        val phoneNumber = et_phone.text.toString()
        val countryCode = aptoPhoneNumberWatcher.countryCode
        showLoading()
        hideKeyboard()
        viewModel.startVerificationUseCase(phoneNumber, countryCode)
    }

    override fun onBackPressed() {
        hideKeyboard()
        delegate?.onBackFromInputPhone()
    }

    companion object {
        fun newInstance(allowedCountriesList: List<Country>) = InputPhoneFragmentThemeTwo().apply {
            val allowedCountries = listOrDefault(allowedCountriesList)
            this.arguments = Bundle().apply { putSerializable(ALLOWED_COUNTRIES_KEY, allowedCountries as Serializable) }
        }

        private fun listOrDefault(allowedCountriesList: List<Country>): List<Country> {
            return if (allowedCountriesList.isNotEmpty()) allowedCountriesList else {
                val defaultCountryList = ArrayList<Country>()
                defaultCountryList.add(Country("US"))
                defaultCountryList
            }
        }
    }
}
