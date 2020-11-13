package com.aptopayments.sdk.repository

internal interface ManageCardIdRepository {
    var data: String?
    fun clear() {
        data = null
    }
}

internal object ManageCardIdRepositoryImpl : ManageCardIdRepository {
    override var data: String? = null
}
