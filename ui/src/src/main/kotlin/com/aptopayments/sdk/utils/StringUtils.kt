package com.aptopayments.sdk.utils

import android.text.Html
import android.text.Spanned
import com.aptopayments.core.data.PhoneNumber
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale
import java.util.regex.Pattern

class StringUtils {

    companion object {
        private val EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+"
        )

        fun parseHtmlLinks(text: String): Spanned {
            return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(text)
            }
        }

        fun capitalizeString(stringToCapitalize: String): String {
            return stringToCapitalize.toUpperCase(Locale.getDefault())[0] +
                    stringToCapitalize.substring(1, stringToCapitalize.length)
        }

        fun isValidEmail(stringToValidate: String): Boolean =
            stringToValidate.isNotEmpty() && EMAIL_ADDRESS_PATTERN.matcher(stringToValidate).matches()

        @Throws(NumberParseException::class)
        fun parsePhoneNumber(phoneNumberStr: String?): PhoneNumber {
            val phoneUtil = PhoneNumberUtil.getInstance()
            // phone must begin with '+'
            val numberProto = phoneUtil.parse(phoneNumberStr, "")
            return PhoneNumber(numberProto.countryCode.toString(), numberProto.nationalNumber.toString())
        }
    }
}
