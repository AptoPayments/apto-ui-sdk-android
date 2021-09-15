package com.aptopayments.sdk.features.inputdata.id

import java.util.regex.Pattern

internal class SSNValidator {

    private val ssnRegex = Pattern.compile("^(?!666|000|9\\d{2})\\d{3}(?!00)\\d{2}(?!0{4})\\d{4}$")

    fun validate(value: String?): Boolean {
        return ssnRegex.matcher(removeHyphensAndSpaces(value)).matches()
    }

    private fun removeHyphensAndSpaces(value: String?) = value?.filter { it != ' ' && it != '-' } ?: ""
}
