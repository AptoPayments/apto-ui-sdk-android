package com.aptopayments.sdk.features.loadfunds.result

import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourceElement

internal data class PaymentResultElement(
    val title: String,
    val status: String,
    val time: String,
    val source: PaymentSourceElement,
    val authorizationId: String,
    val transactionLegend: String
)
