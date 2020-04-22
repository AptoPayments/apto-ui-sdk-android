package com.aptopayments.sdk.features.transactiondetails

import com.aptopayments.core.analytics.Event
import com.aptopayments.core.data.transaction.Transaction
import com.aptopayments.core.extension.formatForTransactionDetails
import com.aptopayments.sdk.core.platform.BaseViewModel
import com.aptopayments.sdk.features.analytics.AnalyticsServiceContract

internal class TransactionDetailsViewModel constructor(
    val transaction: Transaction,
    private val analyticsManager: AnalyticsServiceContract
) : BaseViewModel() {

    val isDeclined by lazy { transaction.state == Transaction.TransactionState.DECLINED }
    val createdAt by lazy { transaction.createdAt.formatForTransactionDetails() }
    val addressName by lazy { calculateAddressName() }
    val mccName by lazy { transaction.merchant?.mcc?.toLocalizedString() }
    val fundingSourceName by lazy { transaction.fundingSourceName }
    val deviceType by lazy { transaction.deviceType().toLocalizedString() }
    val transactionType by lazy { transaction.transactionType.toLocalizedString() }
    val transactionStatus by lazy { transaction.state.toLocalizedString() }
    val declinedDescription by lazy { transaction.declineCode?.toLocalizedString() }
    val transactionDescription by lazy { transaction.transactionDescription }
    val localAmountRepresentation by lazy { transaction.getLocalAmountRepresentation() }
    val nativeBalanceRepresentation by lazy { calculateNativeBalanceRepresentation() }

    fun viewLoaded() {
        analyticsManager.track(Event.TransactionDetail)
    }

    private fun calculateAddressName(): String? {
        val address = transaction.store?.address
        val representation = address?.toStringRepresentation()
        val string = if (representation != address?.country?.name) representation else null
        return string ?: ""
    }

    private fun calculateNativeBalanceRepresentation(): String {
        return if (transaction.getNativeBalanceRepresentation().isNotBlank() && transaction.localAmount?.currency != transaction.nativeBalance?.currency) {
            "≈ ${transaction.getNativeBalanceRepresentation()}"
        } else {
            ""
        }
    }
}
