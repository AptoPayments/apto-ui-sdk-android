package com.aptopayments.sdk.features.loadfunds.paymentsources.list

import com.aptopayments.mobile.data.paymentsources.PaymentSource
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourceElement

internal data class PaymentSourcesListItem(
    val elem: PaymentSourceElement,
    val isPreferred: Boolean,
    val type: Type,
    val source: PaymentSource? = null
) {

    internal enum class Type(val value: Int) {
        EXISTING(0), NEW(1)
    }
}
