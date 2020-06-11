package com.aptopayments.sdk.features.selectcountry

import com.aptopayments.core.data.geo.Country
import com.aptopayments.sdk.core.platform.FragmentDelegate

interface CountrySelectorContract {

    interface Delegate : FragmentDelegate {
        fun onBackFromCountrySelector()
        fun onCountrySelected(country: Country)
    }

    interface View {
        var delegate: Delegate?
    }
}
