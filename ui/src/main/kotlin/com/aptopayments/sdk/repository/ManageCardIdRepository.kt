package com.aptopayments.sdk.repository

interface ManageCardIdRepository {
    var data: String?
    fun clear() {
        data = null
    }
}

object ManageCardIdRepositoryImpl : ManageCardIdRepository {
    override var data: String? = null
}
