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

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
        val isValid = isValidInput(charSequence.toString())
        callback.onValidInput(isValid)
    }

    private fun isValidInput(input: String) =
        isMinLengthValid(input) &&
                (regexValidator?.let { it.toRegex().matches(input) && isMinLengthValid(input) } ?: true)

    private fun isMinLengthValid(input: String) = input.length >= minNumberCharacters

    override fun afterTextChanged(string: Editable) {}

    private fun setMaxCharacters() {
        val filterArray = arrayOf<InputFilter>(InputFilter.LengthFilter(minNumberCharacters))
        inputText.filters = filterArray
    }
}
