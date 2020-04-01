package com.aptopayments.sdk.utils

import android.text.Html
import android.text.Spanned
import android.util.Patterns
import com.aptopayments.core.data.PhoneNumber
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale

class StringUtils {

    companion object {

        fun parseHtmlLinks(text: String): Spanned {
            // Parse HTML links in text and make them clickable
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(text)
            }
        }

        fun capitalizeString(stringToCapitalize: String): String {
            return stringToCapitalize.toUpperCase(Locale.getDefault())[0] + stringToCapitalize.substring(1, stringToCapitalize.length)
        }

        fun isValidEmail(stringToValidate: String): Boolean = stringToValidate.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(stringToValidate).matches()

        @Throws(NumberParseException::class)
        fun parsePhoneNumber(phoneNumberStr: String?): PhoneNumber {
            val phoneUtil = PhoneNumberUtil.getInstance()
            // phone must begin with '+'
            val numberProto = phoneUtil.parse(phoneNumberStr, "")
            return PhoneNumber(numberProto.countryCode.toString(), numberProto.nationalNumber.toString())
        }
    }
}
