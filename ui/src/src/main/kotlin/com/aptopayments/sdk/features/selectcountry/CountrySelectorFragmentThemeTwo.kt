package com.aptopayments.sdk.features.selectcountry

import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aptopayments.core.data.config.UIConfig
import com.aptopayments.core.data.geo.Country
import com.aptopayments.sdk.R
import com.aptopayments.sdk.core.extension.ToolbarConfiguration
import com.aptopayments.sdk.core.extension.configure
import com.aptopayments.sdk.core.extension.setBackgroundColorKeepShape
import com.aptopayments.sdk.core.platform.BaseFragment
import com.aptopayments.sdk.core.platform.theme.themeManager
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.fragment_country_picker_theme_two.*
import kotlinx.android.synthetic.main.include_toolbar_two.*
import java.io.Serializable
import kotlin.properties.Delegates

private const val ALLOWED_COUNTRIES_KEY = "ALLOWED_COUNTRIES"

internal class CountrySelectorFragmentThemeTwo : BaseFragment(), CountrySelectorContract.View,
    CountryListAdapter.Delegate {

    // Wrapper class required to be able to use Delegates.observable because the Delegates do not support null as the
    // default value of the property (null cannot be observed).
    private class CountryWrapper(var country: Country?)

    private lateinit var allowedCountriesList: List<Country>
    private var countryList: List<CountryListItem> = ArrayList()
    private lateinit var countryListAdapter: CountryListAdapter
    private var selectedCountry: CountryWrapper by Delegates.observable(CountryWrapper(null)) { _, _, new ->
        val isCountrySelected = new.country != null
        updateButtonEnableState(isCountrySelected)
    }
    override var delegate: CountrySelectorContract.Delegate? = null

    override fun layoutId(): Int = R.layout.fragment_country_picker_theme_two

    override fun backgroundColor(): Int = UIConfig.uiBackgroundPrimaryColor

    override fun setUpArguments() {
        allowedCountriesList = arguments!![ALLOWED_COUNTRIES_KEY] as List<Country>
    }

    override fun setupViewModel() {
        selectedCountry = CountryWrapper(null)
    }

    override fun setupUI() {
        setupTheme()
        setupToolBar()
        setupRecyclerView()
    }

    override fun setupListeners() {
        super.setupListeners()
        tv_select_country.setOnClickListener {
            selectedCountry.country?.let { country ->
                delegate?.onCountrySelected(country)
            }
        }
    }

    override fun onCountryTapped(selectedCountry: Country) {
        this.selectedCountry = CountryWrapper(selectedCountry)
        countryList.forEach {
            it.isSelected = it.country == selectedCountry
        }
        countryListAdapter.notifyDataSetChanged()
    }

    private fun updateButtonEnableState(isCountrySelected: Boolean) {
        tv_select_country?.isEnabled = isCountrySelected
        val backgroundColor = if (isCountrySelected) UIConfig.uiPrimaryColor else UIConfig.uiPrimaryColorDisabled
        tv_select_country?.setBackgroundColorKeepShape(backgroundColor)
    }

    private fun setupTheme() {
        with(themeManager()) {
            customizeSecondaryNavigationToolBar(tb_llsdk_toolbar_layout as AppBarLayout)
            customizeLargeTitleLabel(tv_country_selector_header)
            customizeFormLabel(tv_country_selector_description)
            customizeSubmitButton(tv_select_country)
        }
    }

    private fun setupToolBar() {
        tb_llsdk_toolbar.configure(this, ToolbarConfiguration.Builder().build())
    }

    private fun setupRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(activity)
        linearLayoutManager.orientation = RecyclerView.VERTICAL
        rv_country_list.layoutManager = linearLayoutManager
        countryList = allowedCountriesList
            .sortedBy { it.name }
            .map { CountryListItem(it, false) }
        countryListAdapter = CountryListAdapter(countryList)
        countryListAdapter.delegate = this
        rv_country_list.adapter = countryListAdapter
    }

    override fun onBackPressed() {
        delegate?.onBackFromCountrySelector()
    }

    companion object {
        fun newInstance(allowedCountriesList: List<Country>) =
            CountrySelectorFragmentThemeTwo().apply {
                this.arguments = Bundle().apply {
                    putSerializable(ALLOWED_COUNTRIES_KEY, allowedCountriesList as Serializable)
                }
            }
    }
}
