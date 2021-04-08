package com.aptopayments.sdk.features.loadfunds.paymentsources.addcard

import androidx.annotation.IntegerRes
import com.aptopayments.sdk.R
import java.util.Locale

private const val VISA_NETWORK_PATTERN = "^4[0-9]{0,}$"
private const val VISA_PATTERN = "^4[0-9]{15}$"
private const val MASTER_NETWORK_PATTERN = "^(?:5[1-5]|2(?!2([01]|20)|7(2[1-9]|3))[2-7])\\d{0,}$"
private const val MASTER_PATTERN = "^(?:5[1-5]|2(?!2([01]|20)|7(2[1-9]|3))[2-7])\\d{14}$"
private const val AMEX_NETWORK_PATTERN = "^3[47][0-9]{0,}$"
private const val AMEX_PATTERN = "^3[47][0-9]{13}$"
private const val DISCOVER_NETWORK_PATTERN =
    "^6(?:011\\d{0,}|5\\d{0,}|4[4-9]\\d{0,}|22(?:1(?:2[6-9]|[3-9]\\d)|[2-8]\\d{2}|9(?:[01]\\d|2[0-5]))\\d{0,})$"
private const val DISCOVER_PATTERN =
    "^6(?:011\\d{12}|5\\d{14}|4[4-9]\\d{13}|22(?:1(?:2[6-9]|[3-9]\\d)|[2-8]\\d{2}|9(?:[01]\\d|2[0-5]))\\d{10})$"
private const val TEST_NETWORK_PATTERN = "^9[0-9]{0,}$"
private const val TEST_PATTERN = "^9[0-9]{15}$"

private const val NORMAL_CARD_MASK = "[0000] [0000] [0000] [0000]"
private const val AMEX_CARD_MASK = "[0000] [000000] [00000]"

internal enum class CardNetwork(
    val networkPattern: String,
    val pattern: String,
    val visualMask: String,
    val maxLength: Int,
    val cvvDigits: Int,
    @IntegerRes val icon: Int
) {
    VISA(VISA_NETWORK_PATTERN, VISA_PATTERN, NORMAL_CARD_MASK, 19, 3, R.drawable.ic_card_visa),
    MASTERCARD(MASTER_NETWORK_PATTERN, MASTER_PATTERN, NORMAL_CARD_MASK, 19, 3, R.drawable.ic_card_mastercard),
    AMEX(AMEX_NETWORK_PATTERN, AMEX_PATTERN, AMEX_CARD_MASK, 17, 4, R.drawable.ic_card_amex),
    DISCOVER(DISCOVER_NETWORK_PATTERN, DISCOVER_PATTERN, NORMAL_CARD_MASK, 19, 3, R.drawable.ic_card_discover),
    TEST(TEST_NETWORK_PATTERN, TEST_PATTERN, NORMAL_CARD_MASK, 19, 3, R.drawable.ic_card_unknown),
    UNKNOWN("", "", NORMAL_CARD_MASK, 19, 0, R.drawable.ic_card_unknown);

    companion object {
        fun fromString(value: String) =
            try {
                valueOf(value.toUpperCase(Locale.US))
            } catch (e: IllegalArgumentException) {
                UNKNOWN
            }
    }
}
