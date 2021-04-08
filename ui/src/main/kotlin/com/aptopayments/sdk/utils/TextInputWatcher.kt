package com.aptopayments.sdk.utils

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.widget.EditText

class TextInputWatcher(
    private var callback: ValidInputListener,
    private var minNumberCharacters: Int,
    private var inputText: EditText,
    private var regexValidator: String? = null
) : TextWatcher {

    init {
        setMaxCharacters()
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        // do nothing
    }

    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
        // do nothing
    }

    private fun isValidInput(input: String) =
        isMinLengthValid(input) &&
            (regexValidator?.let { it.toRegex().matches(input) && isMinLengthValid(input) } ?: true)

    private fun isMinLengthValid(input: String) = input.length >= minNumberCharacters

    override fun afterTextChanged(editable: Editable) {
        val isValid = isValidInput(editable.toString())
        callback.onValidInput(isValid)
    }

    private fun setMaxCharacters() {
        val filterArray = arrayOf<InputFilter>(InputFilter.LengthFilter(minNumberCharacters))
        inputText.filters = filterArray
    }
}
