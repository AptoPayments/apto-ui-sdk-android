package com.aptopayments.sdk.repository

import com.aptopayments.sdk.data.InitializationData

internal interface InitializationDataRepository {
    var data: InitializationData?

    fun clearData()
}

internal class InMemoryInitializationDataRepository(
    override var data: InitializationData? = null
) : InitializationDataRepository {

    override fun clearData() {
        data = null
    }
}
