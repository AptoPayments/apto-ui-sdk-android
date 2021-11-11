package com.aptopayments.sdk.utils.extensions

import android.annotation.SuppressLint
import android.text.Html
import android.text.Spanned
import androidx.annotation.RestrictTo
import com.aptopayments.mobile.data.PhoneNumber
import com.aptopayments.mobile.extension.localized
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.util.Locale
import java.util.regex.Pattern

private const val VALUE = "VALUE"
private val EMAIL_ADDRESS_PATTERN = Pattern.compile(
    "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
        "\\@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
        "(" + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+"
)

@SuppressLint("DefaultLocale")
internal fun String.toCapitalized(): String =
    this.lowercase(Locale.getDefault()).split(' ').joinToString(" ") { it.capitalize() }

internal fun String.isValidEmail(): Boolean = isNotEmpty() && EMAIL_ADDRESS_PATTERN.matcher(this).matches()

@Suppress("DEPRECATION")
internal fun String.parseHtmlLinks(): Spanned {
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
}

@Throws(NumberParseException::class)
internal fun String.parsePhoneNumber(): PhoneNumber {
    val phoneUtil = PhoneNumberUtil.getInstance()
    val numberProto = phoneUtil.parse(this, "")
    return PhoneNumber(numberProto.countryCode.toString(), numberProto.nationalNumber.toString())
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun String.setValue(value: String, key: String = VALUE): String = this.replaceFirst("<<$key>>", value)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun String.localizeAndSetValue(value: String): String = this.localized().setValue(value)

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun String.localizeAndSetValue(value: String, key: String): String = this.localized().setValue(value, key)
