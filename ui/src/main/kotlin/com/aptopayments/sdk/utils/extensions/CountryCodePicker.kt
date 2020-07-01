package com.aptopayments.sdk.utils.extensions

import com.hbb20.CountryCodePicker

fun CountryCodePicker.disable(disable: Boolean) {
    setCcpClickable(!disable)
    if (disable) {
        setArrowSize(1)
    }
}
