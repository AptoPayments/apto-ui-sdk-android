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
            val inputToValidate = charSequence.toString()
        regexValidator?.let {
            callback.onValidInput(it.toRegex().matches(inputToValidate) && charSequence.toString().length >= minNumberCharacters)
        } ?: callback.onValidInput(inputToValidate.length >= minNumberCharacters)
    }

    override fun afterTextChanged(string: Editable) {}

    private fun setMaxCharacters() {
        val filterArray = arrayOfNulls<InputFilter>(1)
        filterArray[0] = InputFilter.LengthFilter(minNumberCharacters)
        inputText.filters = filterArray
    }

}
