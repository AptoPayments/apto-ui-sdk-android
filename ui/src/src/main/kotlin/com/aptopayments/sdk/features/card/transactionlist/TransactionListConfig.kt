package com.aptopayments.sdk.features.card.transactionlist

import com.aptopayments.core.data.transaction.MCC
import org.threeten.bp.LocalDate
import java.io.Serializable

class TransactionListConfig(
        val startDate: LocalDate?,
        val endDate: LocalDate?,
        val mcc: MCC
) : Serializable
