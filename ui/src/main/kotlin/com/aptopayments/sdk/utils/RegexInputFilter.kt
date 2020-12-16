package com.aptopayments.sdk.utils

import android.text.InputFilter
import android.text.Spanned

private const val NAME_REGEX = "[\\p{L}\\s]+"

open class RegexInputFilter(private val regex: Regex, private val onFail: (() -> Unit)?) : InputFilter {
    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int
    ): CharSequence? {
        return if (source.isNullOrEmpty() || regex.matches(source)) {
            null
        } else {
            onFail?.invoke()
            source.filter { regex.matches(it.toString()) }
        }
    }
}

class NameInputFilter(onFail: (() -> Unit)?) : RegexInputFilter(
    NAME_REGEX.toRegex(), onFail
)
