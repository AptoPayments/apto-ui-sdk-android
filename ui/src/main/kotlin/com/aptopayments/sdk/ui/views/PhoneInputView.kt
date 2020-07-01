package com.aptopayments.sdk.ui.views

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import com.aptopayments.mobile.data.config.UIConfig
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.aptopayments.sdk.utils.AptoPhoneNumberWatcher
import com.aptopayments.sdk.utils.ValidInputListener
import com.aptopayments.sdk.utils.extensions.disable
import kotlinx.android.synthetic.main.view_phone_input.view.*

class PhoneInputView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr) {

    private lateinit var aptoPhoneNumberWatcher: AptoPhoneNumberWatcher
    var delegate: Delegate? = null

    var phoneNumber: String = ""
        private set(value) {
            field = value
            delegate?.onPhoneInputChanged(value, isValid)
        }

    var countryCode: String = ""
        private set(value) {
            field = value
            delegate?.onCountryChanged(value)
        }

    var isValid = false

    interface Delegate {
        fun onPhoneInputChanged(phoneNumber: String, valid: Boolean)
        fun onCountryChanged(countryCode: String)
    }

    init {
        inflate(context, R.layout.view_phone_input, this)
        et_phone.hint = "auth_input_phone_hint".localized()
        country_code_picker.contentColor = UIConfig.textSecondaryColor

        with(themeManager()) {
            customizeEditText(et_phone)
        }
        country_code_picker.setDialogBackgroundColor(UIConfig.uiBackgroundSecondaryColor)
        country_code_picker.setDialogSearchEditTextTintColor(UIConfig.textPrimaryColor)
        country_code_picker.setDialogTextColor(UIConfig.textPrimaryColor)
        background = ContextCompat.getDrawable(context, R.drawable.rounded_corners)
        orientation = HORIZONTAL
    }

    fun setAllowedCountriesIsoCode(allowedCountries: List<String>) {
        val validInputListener = object : ValidInputListener {
            override fun onValidInput(isValid: Boolean) {
                this@PhoneInputView.isValid = isValid
                phoneNumber = et_phone.text.toString()
            }
        }
        aptoPhoneNumberWatcher = AptoPhoneNumberWatcher(allowedCountries.first(), validInputListener)
        et_phone.addTextChangedListener(aptoPhoneNumberWatcher)
        country_code_picker.setOnCountryChangeListener {
            country_code_picker?.let {
                aptoPhoneNumberWatcher.countryCode = country_code_picker.selectedCountryNameCode
                countryCode = country_code_picker.selectedCountryNameCode
            }
        }

        country_code_picker.setDefaultCountryUsingNameCode(allowedCountries.first())
        country_code_picker.setCustomMasterCountries(TextUtils.join(",", allowedCountries))
        country_code_picker.disable(allowedCountries.size == 1)
        preselectCountryIfItsInTheList(allowedCountries)
    }

    private fun preselectCountryIfItsInTheList(allowedCountries: List<String>) {
        country_code_picker.setAutoDetectedCountry(true)
        val selected = country_code_picker.selectedCountryNameCode
        if (!allowedCountries.contains(selected)) {
            country_code_picker.resetToDefaultCountry()
        }
    }

    fun requestPhoneFocus() {
        et_phone.requestFocus()
    }

    fun setPhone(code: String, number: String) {
        country_code_picker.setCountryForPhoneCode(code.toInt())
        et_phone.setText(number)
    }
}
