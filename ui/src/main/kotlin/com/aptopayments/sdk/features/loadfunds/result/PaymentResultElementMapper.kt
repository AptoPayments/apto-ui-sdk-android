package com.aptopayments.sdk.features.loadfunds.result

import com.aptopayments.mobile.data.payment.Payment
import com.aptopayments.mobile.data.payment.PaymentStatus
import com.aptopayments.mobile.data.paymentsources.PaymentSource
import com.aptopayments.mobile.extension.localized
import com.aptopayments.sdk.features.loadfunds.paymentsources.PaymentSourceElementMapper
import com.aptopayments.sdk.utils.extensions.formatForTransactionDetails
import com.aptopayments.sdk.utils.extensions.setValue

internal class PaymentResultElementMapper(private val paymentSourceMapper: PaymentSourceElementMapper) {

    fun map(
        payment: Payment,
        softDescriptor: String?
    ): PaymentResultElement {
        return PaymentResultElement(
            title = getTitle(payment),
            status = payment.status.legend.localized(),
            time = payment.createdAt.formatForTransactionDetails(),
            source = getPaymentSource(payment.source),
            authorizationId = payment.approvalCode,
            transactionLegend = getTransactionLegend(softDescriptor ?: "")
        )
    }

    private fun getTransactionLegend(companyName: String): String {
        return "load_funds_transaction_bank_description".localized().setValue(companyName)
    }

    private fun getPaymentSource(paymentSource: PaymentSource) = paymentSourceMapper.map(paymentSource)

    private fun getTitle(payment: Payment): String {
        return if (payment.status == PaymentStatus.PROCESSED) {
            "load_funds_transaction_successful_description".localized().setValue(payment.amount.toAbsString())
        } else {
            "load_funds_transaction_transfer_initiated".localized()
        }
    }
}
