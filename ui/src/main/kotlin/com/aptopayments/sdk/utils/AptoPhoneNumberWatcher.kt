package com.aptopayments.sdk.utils

import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.Selection
import com.google.i18n.phonenumbers.AsYouTypeFormatter
import com.google.i18n.phonenumbers.PhoneNumberUtil

class AptoPhoneNumberWatcher(countryCode: String, private val listener: ValidInputListener) :
    PhoneNumberFormattingTextWatcher() {

    private var defaultCountryCode = "GB"
    private var regionCode: String
    private var currentNumber: CharSequence = ""
    private var countryCodeForRegion: Int

    private var mSelfChange = false
    private var mStopFormatting: Boolean = false

    private val phoneNumberUtil = PhoneNumberUtil.getInstance()
    private val mFormatter: AsYouTypeFormatter

    var countryCode: String = defaultCountryCode
        set(value) {
            field = value
            countryCodeForRegion = phoneNumberUtil.getCountryCodeForRegion(value)
            regionCode = if (countryCodeForRegion != 1) "+$countryCodeForRegion " else ""
            onTextChanged(currentNumber, 0, 0, 0)
        }

    init {
        this.countryCode = if (countryCode.isNotEmpty()) countryCode else defaultCountryCode
        mFormatter = phoneNumberUtil.getAsYouTypeFormatter(countryCode)
        countryCodeForRegion = phoneNumberUtil.getCountryCodeForRegion(countryCode)
        regionCode = if (countryCodeForRegion != 1) "+$countryCodeForRegion " else ""
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
        if (mSelfChange || mStopFormatting) {
            return
        }
        // If the user manually deleted any non-dialable characters, stop formatting
        if (count > 0 && hasSeparator(s, start, count) && after > 0) {
            stopFormatting()
        }
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
        if (mSelfChange || mStopFormatting) {
            return
        }
        // If the user inserted any non-dialable characters, stop formatting
        if (count == 1 && hasSeparator(s, start, count)) {
            stopFormatting()
        }

        try {
            currentNumber = s
            val phoneNumber = phoneNumberUtil.parse(s, countryCode)
            listener.onValidInput(
                phoneNumber.countryCode == countryCodeForRegion && phoneNumberUtil.isValidNumber(phoneNumber)
            )
        } catch (exception: Throwable) {
            listener.onValidInput(false)
        }
    }

    @Synchronized
    override fun afterTextChanged(s: Editable) {
        if (mStopFormatting) {
            // Restart the formatting when all texts were clear.
            mStopFormatting = s.isNotEmpty()
            return
        }
        if (mSelfChange || s.isEmpty()) {
            // Ignore the change caused by s.replace() or empty strings.
            return
        }
        mSelfChange = true
        s.insert(0, regionCode)
        val formatted = reformat(s, Selection.getSelectionEnd(s))
        if (formatted != null) {
            var rememberedPos = mFormatter.rememberedPosition
            s.replace(0, s.length, formatted)
            // The text could be changed by other TextWatcher after we changed it. If we found the
            // text is not the one we were expecting, just give up calling setSelection().
            if (formatted == s.toString()) {
                rememberedPos -= removeRegionCode(s, regionCode)
                if (rememberedPos > 0) {
                    Selection.setSelection(s, rememberedPos)
                }
            } else {
                removeRegionCode(s, regionCode)
            }
        }
        mSelfChange = false
    }

    private fun removeRegionCode(s: Editable, regionCode: String): Int {
        if (s.startsWith(regionCode)) {
            s.replace(0, regionCode.length, "")
            return regionCode.length
        } else {
            val trimmed = regionCode.trim()
            if (s.startsWith(trimmed)) {
                s.replace(0, trimmed.length, "")
                return trimmed.length
            }
        }
        return 0
    }

    private fun reformat(s: CharSequence, cursor: Int): String? {
        // The index of char to the leftward of the cursor.
        val curIndex = cursor - 1
        var formatted: String? = null
        mFormatter.clear()
        var lastNonSeparator: Char = 0.toChar()
        var hasCursor = false
        val len = s.length
        for (i in 0 until len) {
            val c = s[i]
            if (PhoneNumberUtils.isNonSeparator(c)) {
                if (lastNonSeparator.toInt() != 0) {
                    formatted = getFormattedNumber(lastNonSeparator, hasCursor)
                    hasCursor = false
                }
                lastNonSeparator = c
            }
            if (i == curIndex) {
                hasCursor = true
            }
        }
        if (lastNonSeparator.toInt() != 0) {
            formatted = getFormattedNumber(lastNonSeparator, hasCursor)
        }
        return formatted
    }

    private fun getFormattedNumber(lastNonSeparator: Char, hasCursor: Boolean): String {
        return if (hasCursor)
            mFormatter.inputDigitAndRememberPosition(lastNonSeparator)
        else
            mFormatter.inputDigit(lastNonSeparator)
    }

    private fun stopFormatting() {
        mStopFormatting = true
        mFormatter.clear()
    }

    private fun hasSeparator(s: CharSequence, start: Int, count: Int): Boolean {
        for (i in start until start + count) {
            val c = s[i]
            if (!PhoneNumberUtils.isNonSeparator(c)) {
                return true
            }
        }
        return false
    }
}
