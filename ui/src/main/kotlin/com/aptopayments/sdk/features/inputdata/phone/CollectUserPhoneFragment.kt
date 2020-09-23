package com.aptopayments.sdk.features.inputdata.phone

import android.os.Bundle
import android.view.View
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.data.user.AllowedCountriesConfiguration
import com.aptopayments.mobile.data.user.PhoneDataPoint
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.extension.observeNotNullable
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.ui.views.PhoneInputView
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_phone_input.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val COUNTRY_CONFIG = "config"
private const val DATAPOINT_PHONE = "DATAPOINT_PHONE"

internal class CollectUserPhoneFragment : BaseFragment(),
    CollectUserPhoneContract.View {

    private var initialValue: PhoneDataPoint? = null
    private val viewModel: CollectUserPhoneViewModel by viewModel()
    override var delegate: CollectUserPhoneContract.Delegate? = null
    private lateinit var config: AllowedCountriesConfiguration

    override fun layoutId() = R.layout.fragment_phone_input

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun setUpArguments() {
        config = requireArguments()[COUNTRY_CONFIG] as AllowedCountriesConfiguration
        initialValue = requireArguments()[DATAPOINT_PHONE] as PhoneDataPoint?
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialValue?.let {
            phone_input?.setPhone(it.phoneNumber.countryCode, it.phoneNumber.phoneNumber)
        }
    }

    override fun onPresented() {
        viewModel.viewLoaded()
        phone_input?.requestPhoneFocus()
        showKeyboard()
    }

    override fun setupUI() {
        applyFontsAndColors()
        setupViews()
        setupToolBar()
        setTexts()
    }

    private fun setTexts() {
        tv_phone_header.localizedText = "collect_user_data_phone_title"
        tv_phone_label.localizedText = "collect_user_data_phone_subtitle"
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
        phone_input.setAllowedCountriesIsoCode(config.allowedCountries.map { it.isoCode })
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
        tb_llsdk_toolbar.configure(this, ToolbarConfiguration.Builder().setPrimaryColors().build())
    }

    override fun setupViewModel() {
        observeNotNullable(viewModel.continueEnabled) { continue_button.isEnabled = it }
        observeNotNullable(viewModel.continuePressed) {
            delegate?.onPhoneEnteredCorrectly(it)
        }
    }

    override fun setupListeners() {
        super.setupListeners()
        continue_button.setOnClickListener { onContinueButtonClicked() }
    }

    private fun onContinueButtonClicked() {
        hideKeyboard()
        viewModel.onContinueClicked()
    }

    override fun onBackPressed() {
        hideKeyboard()
        delegate?.onBackFromCollectPhone()
    }

    override fun viewLoaded() = viewModel.viewLoaded()

    companion object {
        fun newInstance(dataPoint: PhoneDataPoint?, config: AllowedCountriesConfiguration, tag: String) =
            CollectUserPhoneFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(DATAPOINT_PHONE, dataPoint)
                    putSerializable(COUNTRY_CONFIG, config)
                }
                TAG = tag
            }
    }
}
