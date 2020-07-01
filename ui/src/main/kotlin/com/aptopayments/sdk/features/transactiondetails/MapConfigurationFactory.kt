package com.aptopayments.sdk.features.transactiondetails

import com.aptopayments.mobile.data.transaction.Transaction
import com.aptopayments.sdk.core.extension.iconResource

internal class MapConfigurationFactory {

    fun create(transaction: Transaction): MapConfiguration? {
        return if (isTransactionCorrectToBeShown(transaction)) {
            val latitude = transaction.store!!.latitude!!
            val longitude = transaction.store!!.longitude!!
            val iconResource = transaction.merchant!!.mcc!!.iconResource
            MapConfiguration(latitude, longitude, iconResource)
        } else {
            null
        }
    }

    private fun isTransactionCorrectToBeShown(transaction: Transaction) =
        transaction.store != null && transaction.store?.latitude != null && transaction.store?.longitude != null && transaction.merchant?.mcc != null
}
